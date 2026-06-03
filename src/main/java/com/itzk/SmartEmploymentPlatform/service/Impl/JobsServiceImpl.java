package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.FavorsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.JobsFavorite;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Resume;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobFavor;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.JobRecommendVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleCompanyVo;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.JobsService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.RedisKeyConstants;
import com.itzk.SmartEmploymentPlatform.utils.RedisUtil;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobsServiceImpl implements JobsService {

    @Autowired
    private JobsMapper jobsMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FavorsMapper favorsMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 职位广场分页列表（求职端首页）
     *
     * 缓存策略：两层设计
     *   1. 共享缓存 — 岗位 + 公司数据，key 不含 userId，所有用户复用，15分钟过期
     *   2. 个性覆盖 — 收藏/已投递状态每次都从 Redis Set 实时读取，不做缓存
     * 这样高并发下只查 Redis 不查 DB，同时每个人的收藏状态保持准确
     */
    @Override
    @Transactional
    public PageResult queryAllJobs(JobQueryDTO jobQueryDTO) {
        jobQueryDTO.setStatus(Byte.valueOf("1"));
        Long userId = UserHolder.getUserId();

        // 从共享缓存获取非个性化数据（jobs + companies）
        JobPageData pageData = getSharedJobPageData(jobQueryDTO);

        // 从 Redis 获取用户个性化标志
        Set<Long> collectJobIdSet = getUserCollectJobIds(userId);
        Set<Long> collectAppJobIdSet = getUserSubmittedJobIds(userId);

        // 组装带用户状态的 VO
        List<JobsFavorite> voList = new ArrayList<>();
        for (Job job : pageData.jobs) {
            Company company = pageData.companyMap.get(job.getCompanyId());
            if (company == null) {
                throw new RuntimeException("不合法数据");
            }
            SimpleCompanyVo companyVo = new SimpleCompanyVo();
            BeanUtils.copyProperties(company, companyVo);

            JobsFavorite vo = new JobsFavorite();
            vo.setJob(job);
            vo.setFavorite(collectJobIdSet.contains(job.getId()));
            vo.setSubmitted(collectAppJobIdSet.contains(job.getId()));
            vo.setCompany(companyVo);
            voList.add(vo);
        }

        return new PageResult(pageData.total, voList);
    }

    /**
     * 收藏职位
     *
     * 写入 MySQL 后同步更新 Redis Set（收藏集合），方便列表页快速判断收藏状态。
     * 防重机制：如果存在冷却锁（deleteFavors 设置的 1 分钟锁），拒绝收藏，防止反复收藏/取消刷数据。
     */
    @Override
    @Transactional
    public Result addJobFavors(JobFavor jobFavor) {
        Long userId = UserHolder.getUserId();
        Long jobId = jobFavor.getJobId();

        // 冷却锁：取消收藏后 1 分钟内不可重新收藏
        String lockKey = RedisKeyConstants.FavorJob_LOCK_PREFIX + userId + ":" + jobId;
        if (!RedisUtil.skipRedis()) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
                    return Result.error("您已取消该职位收藏，需等待1分钟后才能再次收藏");
                }
                RedisUtil.markSuccess();
            } catch (Exception e) {
                log.warn("Redis 查询冷却锁失败，放行: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        int cow = favorsMapper.insert(jobFavor);
        if (cow == 0) {
            return Result.error("收藏职位失败");
        }
        // 同步写 Redis，保证列表页收藏状态即时可见
        if (!RedisUtil.skipRedis()) {
            try {
                stringRedisTemplate.opsForSet()
                        .add(RedisKeyConstants.USER_COLLECT_PREFIX + userId, String.valueOf(jobId));
                stringRedisTemplate.expire(RedisKeyConstants.USER_COLLECT_PREFIX + userId,
                        RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("Redis 同步收藏状态失败: {}", e.getMessage());
            }
        }

        return Result.success();
    }

    /**
     * 取消收藏
     *
     * 除了删 MySQL + Redis，还会设置一个 1 分钟冷却锁，
     * 防止用户反复收藏/取消来刷接口或制造脏数据。
     */
    @Override
    public Result deleteFavors(Long jobId) {

        Long userId = UserHolder.getUserId();

        int row = favorsMapper.deleteByJobIdAndUserId(jobId, userId);
        if (row == 0) {
            return Result.error("职位取消收藏失败");
        }
        if (!RedisUtil.skipRedis()) {
            try {
                stringRedisTemplate.opsForSet()
                        .remove(RedisKeyConstants.USER_COLLECT_PREFIX + userId, String.valueOf(jobId));
                stringRedisTemplate.expire(RedisKeyConstants.USER_COLLECT_PREFIX + userId,
                        RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("Redis 同步取消收藏状态失败: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        // 冷却锁：1 分钟内不可重新收藏
        if (!RedisUtil.skipRedis()) {
            try {
                String lockKey = RedisKeyConstants.FavorJob_LOCK_PREFIX + userId + ":" + jobId;
                stringRedisTemplate.opsForValue().set(lockKey, "1", 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 设置冷却锁失败: {}", e.getMessage());
            }
        }

        return Result.success();
    }

    /**
     * 职位浏览记录
     *
     * 每日去重：同一用户同一天浏览同一职位只记一次浏览量。
     * 用 Redis Set 记录"用户今天浏览过哪些职位"，过期时间设到当天 24:00，
     * 第二天 Set 自动清空，浏览量可以重新计入。
     */
    @Override
    public Result addJobBrowse(Long userId, Long jobId) {
        String userViewKey = RedisKeyConstants.USER_DAILY_VIEW_PREFIX + userId;

        boolean isViewed = false;
        if (!RedisUtil.skipRedis()) {
            try {
                isViewed = Boolean.TRUE.equals(
                        stringRedisTemplate.opsForSet().isMember(userViewKey, jobId.toString()));
            } catch (Exception e) {
                log.warn("Redis 查询浏览记录失败，放行: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        if (!isViewed) {
            jobsMapper.addViewCountById(jobId);
            // 当天首次浏览，写入 Set 并设过期到午夜
            if (!RedisUtil.skipRedis()) {
                try {
                    stringRedisTemplate.opsForSet().add(userViewKey, jobId.toString());
                    stringRedisTemplate.expire(userViewKey, getSecondsUntilEndOfDay(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("Redis 写入浏览记录失败: {}", e.getMessage());
                }
            }
        }

        return Result.success();
    }

    /**
     * 获取当前用户全部收藏岗位
     *
     * 收藏 ID 集合由 getUserCollectJobIds 统一管理（Redis 优先，MySQL 兜底重建）。
     * 已投递状态从 Redis Set 实时读取，不做缓存。
     */
    @Override
    public Result getAllFavorJobs(Long userId) {
        // Redis 优先，空时自动从 MySQL 重建
        Set<Long> collectJobIdSet = getUserCollectJobIds(userId);
        if (collectJobIdSet.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 已投递集合（实时读 Redis，不缓存）
        Set<Long> submittedSet = getUserSubmittedJobIds(userId);

        List<Job> jobList = jobsMapper.selectBatchIds(collectJobIdSet);
        List<JobsFavorite> voList = new ArrayList<>();
        for (Job job : jobList) {
            JobsFavorite vo = new JobsFavorite();
            vo.setJob(job);
            vo.setSubmitted(submittedSet.contains(job.getId()));
            voList.add(vo);
        }

        return Result.success(voList);
    }

    /** 推荐结果数量，修改这里全局生效 */
    private static final int RECOMMENDED_COUNT = 6;

    /**
     * 智能推荐岗位
     *
     * 匹配逻辑：工作标签 → 匹配人（岗位的 job_skills 有多少命中用户简历中的 skills）
     *   匹配度 = 命中用户技能的岗位标签数 / 岗位标签总数 × 100%
     *   匹配方式：精确相等 > 双向包含（如 "Spring Boot" 匹配 "Spring"）
     *
     * 随机化策略（防止每次返回相同的 top N）：
     *   - 结果 ≤ RECOMMENDED_COUNT：打乱顺序即可
     *   - 结果 > RECOMMENDED_COUNT：高分池随机取 1/3，中低分池随机补齐到目标数
     */
    @Override
    public Result<List<JobRecommendVO>> recommendedJobs(Long userId) {

        List<JobRecommendVO> voList = new ArrayList<>();

        // 1. 从默认简历提取用户技能标签
        Resume r = resumeMapper.getDefaultByuserId(userId);
        if (r == null || r.getSkills() == null || r.getSkills().trim().isEmpty()) {
            return randomRecommend();
        }

        // 兼容英文逗号、中文逗号、顿号等分隔符
        List<String> userSkills = new ArrayList<>();
        for (String s : r.getSkills().split("[,，、]")) {
            String t = s.trim();
            if (!t.isEmpty()) userSkills.add(t);
        }
        if (userSkills.isEmpty()) {
            return randomRecommend();
        }

        // 2. SQL 模糊匹配职位（LIKE 方式，兼容各种格式）
        List<Job> jobList = jobsMapper.getjobsBySkill(userSkills.toArray(new String[0]));
        if (jobList == null || jobList.isEmpty()) {
            return randomRecommend();
        }

        // 3. 计算每个岗位的匹配度（岗位标签 → 用户技能）
        for (Job job : jobList) {
            int matchCount = 0;
            String jobSkills = job.getJobSkills();
            List<String> jobTags = new ArrayList<>();
            if (jobSkills != null) {
                for (String tag : jobSkills.split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) jobTags.add(t);
                }
            }
            int totalJobTags = jobTags.size();
            if (totalJobTags > 0) {
                for (String jobTag : jobTags) {
                    for (String userSkill : userSkills) {
                        if (jobTag.equalsIgnoreCase(userSkill)
                                || jobTag.toLowerCase().contains(userSkill.toLowerCase())
                                || userSkill.toLowerCase().contains(jobTag.toLowerCase())) {
                            matchCount++;
                            break;
                        }
                    }
                }
            }
            JobRecommendVO vo = new JobRecommendVO();
            vo.setJob(job);
            vo.setMatchScore(totalJobTags > 0 ? (int) Math.round((matchCount * 100.0) / totalJobTags) : 0);
            voList.add(vo);
        }

        // 4. 按匹配度降序
        voList.sort(Comparator.comparingInt(JobRecommendVO::getMatchScore).reversed());

        // 5. 随机化选取（高分池 + 中低分池混合）
        int size = voList.size();
        int highPick = Math.max(1, RECOMMENDED_COUNT / 3);
        List<JobRecommendVO> result = new ArrayList<>();

        if (size <= RECOMMENDED_COUNT) {
            result.addAll(voList);
            // 不足时用随机职位补位
            if (size < RECOMMENDED_COUNT) {
                Set<Long> existing = voList.stream().map(v -> v.getJob().getId()).collect(Collectors.toSet());
                for (Job job : jobsMapper.getJobRandom()) {
                    if (result.size() >= RECOMMENDED_COUNT) break;
                    if (existing.contains(job.getId())) continue;
                    JobRecommendVO vo = new JobRecommendVO();
                    vo.setJob(job);
                    vo.setMatchScore(0);
                    result.add(vo);
                }
            }
            Collections.shuffle(result);
        } else {
            int splitIdx = Math.max(highPick, size / 3);
            List<JobRecommendVO> highPool = new ArrayList<>(voList.subList(0, splitIdx));
            List<JobRecommendVO> restPool = new ArrayList<>(voList.subList(splitIdx, size));

            Collections.shuffle(highPool);
            Collections.shuffle(restPool);

            result.addAll(highPool.subList(0, Math.min(highPick, highPool.size())));
            int need = RECOMMENDED_COUNT - result.size();
            if (restPool.size() >= need) {
                result.addAll(restPool.subList(0, need));
            } else {
                result.addAll(restPool);
            }
            Collections.shuffle(result);
        }

        return Result.success(result);
    }

    /** 纯随机推荐（无简历/无技能时的兜底策略） */
    private Result<List<JobRecommendVO>> randomRecommend() {
        List<JobRecommendVO> list = new ArrayList<>();
        for (Job job : jobsMapper.getJobRandom()) {
            JobRecommendVO vo = new JobRecommendVO();
            vo.setJob(job);
            vo.setMatchScore(0);
            list.add(vo);
        }
        Collections.shuffle(list);
        return Result.success(list);
    }

    @Override
    public PageResult queryAlCompanyJobs(JobQueryDTO jobQueryDTO) {
        Long companyId = UserHolder.getCompanyId();
        LocalDateTime start =null;
        LocalDateTime end =null;
        LocalDate dateFrom = jobQueryDTO.getDateFrom();
        LocalDate dateTo = jobQueryDTO.getDateTo();
        if(jobQueryDTO.getDateFrom() != null && jobQueryDTO.getDateTo() != null){
            start = dateFrom.atStartOfDay();        // 2026-06-18 00:00:00
            end = dateTo.atTime(23, 59, 59);
        }

        PageHelper.startPage(jobQueryDTO.getPage(),jobQueryDTO.getSize());
        //根据公司搜索
        Page<Job> page = jobsMapper.getALLJobBuCompanyId(jobQueryDTO,companyId,start,end);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 添加/编辑职位
     *
     * 创建：status=0（审核中），pending_adjust=1，company.job_count 等审核后 +1。
     * HR 表单编辑（带 delta）：delta 立即应用到 jobs 表，pending_adjust 累积，
     *    status 回到 0（审核中），company.job_count 等审核后再变动。
     * 简单更新（无 delta，如上下架切换）：仅更新传入字段，不动 pending_adjust。
     */
    @OperationLog(action = "JOB_PUBLISH", targetType = "job", targetId = "#job.id",
                  targetName = "#job.title")
    @Override
    public Result addJob(JobDTO job) {
        log.info("添加职位{}", job);
        LocalDateTime now = LocalDateTime.now();

        if (job.getId() == null) {
            // 新建：status=0（审核中），pending_adjust=1
            Long userId = UserHolder.getUserId();
            Long companyId = UserHolder.getCompanyId();
            job.setHrUserId(userId);
            job.setCompanyId(companyId);
            job.setStatus(Constant.JobStatus.REVIEWING);
            job.setViewCount(0);
            job.setApplyCount(0);
            job.setHasCount(job.getHeadcount());
            job.setCreatedAt(now);
            job.setUpdatedAt(now);
            job.setPendingAdjust(1);
            jobsMapper.insert(job);
        } else {
            Job existing = jobsMapper.getJobBy(job.getId());
            Integer delta = job.getDelta();

            if (delta != null) {
                // HR 表单编辑：有 delta → 验证并累积 pending_adjust，status 回到审核中
                if (delta < 0 && Math.abs(delta) > existing.getHasCount()) {
                    return Result.error("减少岗位数不能超过当前在招岗位数(" + existing.getHasCount() + ")");
                }
                job.setHeadcount(delta);
                job.setHasCount(delta);
                job.setStatus(Constant.JobStatus.REVIEWING);
                job.setPendingAdjust(existing.getPendingAdjust() + delta);
            }
            // delta == null → 简单操作（上下架切换等），仅更新传入字段

            job.setUpdatedAt(now);
            jobsMapper.updata(job);
        }

        try {
            stringRedisTemplate.delete(stringRedisTemplate.keys(RedisKeyConstants.JOBS_LIST_CACHE_PREFIX + "*"));
            stringRedisTemplate.delete(stringRedisTemplate.keys(RedisKeyConstants.JOBS_HOT_CACHE_PREFIX + "*"));
        } catch (Exception e) {
            log.warn("清除职位缓存失败: {}", e.getMessage());
        }

        return Result.success();
    }


    /**
     * 计算当前时间到当天23:59:59的剩余秒数
     */
    private long getSecondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        return ChronoUnit.SECONDS.between(now, endOfDay);
    }

    /**
     * 获取用户收藏职位 ID 集合
     *
     * 先查 Redis Set，为空或异常时从 MySQL 查询并回写 Redis（懒重建），
     * 避免 Redis 故障导致收藏状态永久丢失。
     */
    private Set<Long> getUserCollectJobIds(Long userId) {
        String key = RedisKeyConstants.USER_COLLECT_PREFIX + userId;
        try {
            Set<String> members = stringRedisTemplate.opsForSet().members(key);
            if (members != null && !members.isEmpty()) {
                return members.stream().map(Long::parseLong).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("Redis 查询收藏集合失败，尝试从 MySQL 重建: {}", e.getMessage());
        }

        // Redis 为空或异常，从 MySQL 重建
        List<Long> ids = favorsMapper.selectJobIdsByUserId(userId);
        if (ids != null && !ids.isEmpty()) {
            log.info("从 MySQL 重建用户 {} 收藏集合，共 {} 条", userId, ids.size());
            try {
                String[] arr = ids.stream().map(String::valueOf).toArray(String[]::new);
                stringRedisTemplate.opsForSet().add(key, arr);
                stringRedisTemplate.expire(key, RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("Redis 回写收藏集合失败: {}", e.getMessage());
            }
            return new HashSet<>(ids);
        }
        return new HashSet<>();
    }

    /**
     * 热门岗位（按投递数 + 浏览量排序）
     *
     * Redis 缓存 15 分钟，key = jobs:hot:{limit}，所有用户共享
     */
    @Override
    public Result<List<Job>> getHotJobs(int limit) {
        String cacheKey = RedisKeyConstants.JOBS_HOT_CACHE_PREFIX + limit;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Result.success(JSON.parseArray(cached, Job.class));
            }
        } catch (Exception e) {
            log.warn("Redis 读取热门岗位缓存失败，降级查 DB: {}", e.getMessage());
        }
        List<Job> jobs = jobsMapper.getHotJobs(limit);
        if (jobs != null && !jobs.isEmpty()) {
            try {
                stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(jobs),
                        RedisKeyConstants.JOBS_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 写入热门岗位缓存失败: {}", e.getMessage());
            }
        }
        return Result.success(jobs);
    }

    /**
     * 从 Redis 获取用户已投递职位 ID 集合（实时，不缓存）
     */
    private Set<Long> getUserSubmittedJobIds(Long userId) {
        try {
            Set<String> members = stringRedisTemplate.opsForSet()
                    .members(RedisKeyConstants.JOB_VIEW_SUBMITTED_PREFIX + userId);
            if (members != null && !members.isEmpty()) {
                return members.stream().map(Long::parseLong).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("Redis 查询已投递集合失败: {}", e.getMessage());
        }
        return new HashSet<>();
    }

    /**
     * 获取岗位列表共享缓存数据（不含用户个性化信息）
     *
     * 缓存的是 PageHelper 分页结果 + 公司 Map，不含收藏/投递状态。
     * Key 不含 userId，所有用户共享，15 分钟过期。
     * 命中后由 queryAllJobs 覆盖个性化标志。
     */
    private JobPageData getSharedJobPageData(JobQueryDTO jobQueryDTO) {
        String cacheKey = RedisKeyConstants.JOBS_LIST_CACHE_PREFIX
                + jobQueryDTO.getPage() + ":" + jobQueryDTO.getSize() + ":"
                + (jobQueryDTO.getKeyword() != null ? jobQueryDTO.getKeyword() : "") + ":"
                + (jobQueryDTO.getCity() != null ? jobQueryDTO.getCity() : "");

        // 尝试读缓存，Redis 不可用时降级查 DB
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return JSON.parseObject(cached, JobPageData.class);
            }
        } catch (Exception e) {
            log.warn("Redis 读取职位列表缓存失败，降级查 DB: {}", e.getMessage());
        }

        PageHelper.startPage(jobQueryDTO.getPage(), jobQueryDTO.getSize());
        Page<Job> page = jobsMapper.listJobs(jobQueryDTO);
        List<Job> jobList = page.getResult();

        // 批量查公司，放入 Map 供后续组装
        Map<Long, Company> companyMap = new HashMap<>();
        for (Job job : jobList) {
            Company company = companyMapper.getByJobId(job.getCompanyId());
            if (company == null) {
                throw new RuntimeException("不合法数据");
            }
            companyMap.put(job.getCompanyId(), company);
        }

        JobPageData data = new JobPageData(page.getTotal(), jobList, companyMap);

        // 仅非空结果写缓存，避免空数据长时间阻塞新职位展示
        if (page.getTotal() > 0) {
            try {
                stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data),
                        RedisKeyConstants.JOBS_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 写入职位列表缓存失败: {}", e.getMessage());
            }
        }
        return data;
    }

    /**
     * 共享缓存的载体：分页总数 + 岗位列表 + 公司 Map
     * 不包含用户个性化字段（favorite/submitted），由调用方覆盖
     */
    public static class JobPageData {
        public long total;
        public List<Job> jobs;
        public Map<Long, Company> companyMap;

        public JobPageData() {}

        public JobPageData(long total, List<Job> jobs, Map<Long, Company> companyMap) {
            this.total = total;
            this.jobs = jobs;
            this.companyMap = companyMap;
        }
    }
}
