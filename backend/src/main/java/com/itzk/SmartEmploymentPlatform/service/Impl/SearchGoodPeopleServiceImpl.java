package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyResumeMapper;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.FavorResumDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Resume;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryResumeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.service.SearchGoodPeopleService;
import com.itzk.SmartEmploymentPlatform.utils.RedisKeyConstants;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchGoodPeopleServiceImpl implements SearchGoodPeopleService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private CompanyResumeMapper companyResumeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @Override
    public Result getAllIntention(QueryResumeDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getSize());
        Page<Resume> page = resumeMapper.getAllIntention(dto);

        return Result.success(new PageResult(page.getTotal(), page.getResult()));
    }

    @Override
    public Result getResumeById(Long resumeId) {
        Resume resume = resumeMapper.getResumeById(resumeId);
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo);

        vo.setEducations(resumeMapper.getEducationsByResumeId(resumeId));
        vo.setExperiences(resumeMapper.getExperiencesByResumeId(resumeId));
        vo.setProjects(resumeMapper.getProjectsByResumeId(resumeId));

        Set<Long> collectResumeIdSet = getCompanyCollectResumeIds(UserHolder.getCompanyId());
        if (collectResumeIdSet.contains(resumeId)) {
            vo.setFavorite((byte) 1);
        }

        return Result.success(vo);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveCompanyResume(FavorResumDTO dto) {
        //在公司收藏表中插入信息
        companyResumeMapper.insert(dto);
        try {
            String key = RedisKeyConstants.FAVORRESUME_PREFIX + dto.getCompanyId();
            stringRedisTemplate.opsForSet().add(key, String.valueOf(dto.getResumeId()));
            stringRedisTemplate.expire(key, RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 同步收藏状态失败: {}", e.getMessage());
        }

        return Result.success();
    }

    @Override
    @Transactional
    public Result cancelCompanyResume(Long resumeId) {

        Long companyId = UserHolder.getCompanyId();

        companyResumeMapper.deleteBycompanyIdAndResumeId(companyId,resumeId);

        try {
            String key = RedisKeyConstants.FAVORRESUME_PREFIX + companyId;
            stringRedisTemplate.opsForSet().remove(key, String.valueOf(resumeId));
            stringRedisTemplate.expire(key, RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 同步取消收藏状态失败: {}", e.getMessage());
        }

        return Result.success();
    }

    /**
     * 获取公司全部收藏对象
     * @return
     */
    @Override
    public Result getFavorites() {
        Long companyId = UserHolder.getCompanyId();
        String savekey = RedisKeyConstants.COLLECT_RESUME_PREFIX + companyId;
        try {
            String json = stringRedisTemplate.opsForValue().get(savekey);
            if (json != null) {
                List<Resume> list = JSON.parseArray(json, Resume.class);
                return Result.success(list);
            }
        } catch (Exception e) {
            log.warn("Redis 读取收藏简历缓存失败，降级查 DB: {}", e.getMessage());
        }

        // 懒修复：Redis 为空时从 MySQL 重建
        Set<Long> idSet = getCompanyCollectResumeIds(companyId);
        if (idSet == null || idSet.isEmpty()) {
            return Result.success();
        }
        List<Long> ids = new ArrayList<>(idSet);

        List<Resume> resumes = resumeMapper.getResumesByIds(ids);
        List<FavorResumDTO> favors = companyResumeMapper.getRemarkByIds(ids);
        List<Resume> list = new ArrayList<>();
        for (Resume r : resumes) {
            for (FavorResumDTO f : favors) {
                if(r.getId() == f.getResumeId()){
                    r.setFavoriteReason(f.getRemark());
                    list.add(r);
                    break;
                }
            }
        }

        //存入缓存
        if (list != null && !list.isEmpty()) {
            try {
                stringRedisTemplate.opsForValue().set(savekey, JSON.toJSONString(list), 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 写入收藏简历缓存失败: {}", e.getMessage());
            }
        }

        return Result.success(list);
    }

    @Override
    public Result updataRemark(Long resumeId, String reason) {
        Long companyId = UserHolder.getCompanyId();
        companyResumeMapper.updataRemark(resumeId,reason,companyId);
        try {
            String savekey = RedisKeyConstants.COLLECT_RESUME_PREFIX + companyId;
            stringRedisTemplate.delete(savekey);
        } catch (Exception e) {
            log.warn("Redis 清除收藏缓存失败: {}", e.getMessage());
        }
        return Result.success();
    }

    /**
     * 获取公司收藏的简历ID集合（懒修复：Redis 为空时从 MySQL 重建）
     */
    private Set<Long> getCompanyCollectResumeIds(Long companyId) {
        String key = RedisKeyConstants.FAVORRESUME_PREFIX + companyId;
        try {
            Set<String> members = stringRedisTemplate.opsForSet().members(key);
            if (members != null && !members.isEmpty()) {
                return members.stream().map(Long::parseLong).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("Redis 查询公司收藏集合失败，尝试从 MySQL 重建: {}", e.getMessage());
        }

        List<Long> ids = companyResumeMapper.selectResumeIdsByCompanyId(companyId);
        if (ids != null && !ids.isEmpty()) {
            log.info("从 MySQL 重建公司 {} 收藏集合，共 {} 条", companyId, ids.size());
            try {
                String[] arr = ids.stream().map(String::valueOf).toArray(String[]::new);
                stringRedisTemplate.opsForSet().add(key, arr);
                stringRedisTemplate.expire(key, RedisKeyConstants.FAVOR_SET_TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("Redis 回写公司收藏集合失败: {}", e.getMessage());
            }
            return new HashSet<>(ids);
        }
        return new HashSet<>();
    }

}
