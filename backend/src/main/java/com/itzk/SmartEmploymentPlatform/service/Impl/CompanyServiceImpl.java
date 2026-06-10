package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryJobsByCompanyIdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyJobsVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleJobDetailVo;
import com.itzk.SmartEmploymentPlatform.service.CompanyService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JobsMapper jobsMapper;




    @Override
    public Map<String, List<String>> getFilterItems() {
        // 1. 查询所有不重复的行业
        List<String> industries = companyMapper.getDistinctIndustries();
        
        // 2. 查询所有不重复的规模
        List<String> sizes = companyMapper.getDistinctSizes();
        
        // 3. 封装成你要的 MAP
        Map<String, List<String>> result = new HashMap<>();
        result.put("industries", industries);
        result.put("sizes", sizes);
        
        return result;
    }

    @Override
    public Result<PageResult> getCompanyList(CompanyQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());

        List<CompanyVO> list = companyMapper.selectCompanyList(queryDTO);

        Page<CompanyVO> page = (Page<CompanyVO>) list;
        PageResult pageResult = new PageResult(page.getTotal(), list);

        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result getCompanyJobsDetail(Long id) {

        //查询公司信息
        Company company = companyMapper.getById(id);
        CompanyVO companyVO = new CompanyVO();
        BeanUtils.copyProperties(company, companyVO);
        //获取公司所有发布职位===还得分页查询
        int start = 0;
        int total = 10;
        List<Job> list = jobsMapper.getJobsByCompanyId(id,start,total);
        List<SimpleJobDetailVo> jobList = new ArrayList<>();
        CompanyJobsVo vo  = new CompanyJobsVo();
        vo.setCompanyVO(companyVO);
        if (list == null|| list.isEmpty()){
            vo.setJobList(jobList);
            return Result.success(vo);
        }


        for (Job job : list) {
            SimpleJobDetailVo simpleJobDetailVo = new SimpleJobDetailVo();
            BeanUtils.copyProperties(job, simpleJobDetailVo);
            jobList.add(simpleJobDetailVo);
        }

        //封装对象并返回


        vo.setJobList(jobList);

        return Result.success(vo);
    }

    @Override
    public Result PageQueryJobsByCompanyId(Long companyId, QueryJobsByCompanyIdDTO query) {
        int i = query.getPage() - 1;
        int page = i*10;
        //分页查询岗位信息
        List<Job> jobList = jobsMapper.getJobsByCompanyId(companyId, page, query.getPageSize());


        return Result.success(jobList);
    }

    /**
     * hr相关接口，获取注册公司状态
     * @param companyId
     * @return
     */

    @Override
    @Transactional
    public Result getCompanyStatus(Long companyId) {
        if (companyId == null || companyId == 0){
            return Result.error("请重新登录获取状态");
        }


        Company company = companyMapper.getById(companyId);


        return Result.success(company);
    }

    /**
     * 更新公司信息（仅允许更新本公司，不允许修改审核状态等敏感字段）
     */
    @Override
    public Result updata(Company company) {
        Long currentCompanyId = UserHolder.getCompanyId();
        if (currentCompanyId == null || currentCompanyId == 0) {
            return Result.error("未绑定企业");
        }
        if (company.getId() == null || !company.getId().equals(currentCompanyId)) {
            return Result.error("只能修改本企业信息");
        }
        // 防止前端传入敏感字段
        company.setAuditStatus(null);
        company.setAuditRemark(null);
        company.setUserId(null);
        company.setJobCount(null);
        company.setJobConfirm(null);
        companyMapper.updataById(company);
        return Result.success();
    }

    /**
     * 重新申请：
     * - status=0(待审核): 仅更新 updatedAt 顶时间,让管理员更早看到
     * - status=2(被拒): 重置为 0 清空拒绝理由
     * 统一加 Redis 24h 锁,一天只能操作一次
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result reapply(Long companyId) {
        if (companyId == null || companyId == 0) {
            return Result.error("公司ID无效，请重新登录");
        }
        Long currentCompanyId = UserHolder.getCompanyId();
        if (currentCompanyId == null || !currentCompanyId.equals(companyId)) {
            return Result.error("无权操作该公司");
        }
        Company c = companyMapper.getById(companyId);
        if (c == null) {
            return Result.error("公司不存在");
        }
        if (c.getAuditStatus() == null
                || c.getAuditStatus() == Constant.AuditStatus.APPROVED) {
            return Result.error("企业已审核通过，无需重新申请");
        }

        // Redis 24h 锁：一天只能操作一次
        String lockKey = RedisKeyConstants.COMPANY_REAPPLY_LOCK_PREFIX + companyId;
        if (!RedisUtil.skipRedis()) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
                    return Result.error("24小时内只能重新申请一次，请稍后再试");
                }
                RedisUtil.markSuccess();
            } catch (Exception e) {
                log.warn("Redis 查询重新申请锁失败，放行: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        Company update = new Company();
        update.setId(companyId);
        update.setUpdatedAt(java.time.LocalDateTime.now());

        if (c.getAuditStatus() == Constant.AuditStatus.REJECTED) {
            // 被拒：重置为待审核 + 清空拒绝理由
            update.setAuditStatus(Constant.AuditStatus.PENDING);
            update.setAuditRemark(null);
        }
        // status=0：仅更新 updatedAt 顶时间

        companyMapper.updataById(update);

        // 加锁 24h
        if (!RedisUtil.skipRedis()) {
            try {
                stringRedisTemplate.opsForValue().set(lockKey, "1", 24, TimeUnit.HOURS);
                RedisUtil.markSuccess();
            } catch (Exception e) {
                log.warn("Redis 设置重新申请锁失败: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        return Result.success();
    }
}