package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminCompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminJobVO;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.AdminJobService;
import com.itzk.SmartEmploymentPlatform.utils.RedisKeyConstants;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminJobServiceImpl implements AdminJobService {

    @Autowired
    private JobsMapper jobsMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageInfo<AdminJobVO> list(int page, int size, String title, String companyName, Integer status) {
        PageHelper.startPage(page, size);
        List<AdminJobVO> list = jobsMapper.selectAdminList(title, companyName, status);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void create(Map<String, Object> body) {
        String title = (String) body.get("title");
        String companyName = (String) body.get("companyName");
        if (title == null || title.isBlank()) throw new BusinessException("职位名称不能为空");
        if (companyName == null || companyName.isBlank()) throw new BusinessException("企业名称不能为空");

        // 按名称查企业，取第一条匹配
        PageHelper.startPage(1, 1);
        List<AdminCompanyVO> companies = companyMapper.selectAdminList(companyName, null, null);
        if (companies.isEmpty()) throw new BusinessException("企业不存在: " + companyName);
        Long companyId = companies.get(0).getId();

        Job job = new Job();
        job.setTitle(title);
        job.setCompanyId(companyId);
        job.setHrUserId(UserHolder.getUserId());
        job.setCategory((String) body.get("category"));
        job.setCity((String) body.get("city"));
        if (body.get("salaryMin") != null) job.setSalaryMin(((Number) body.get("salaryMin")).intValue());
        if (body.get("salaryMax") != null) job.setSalaryMax(((Number) body.get("salaryMax")).intValue());
        if (body.get("education") != null) job.setEducation(((Number) body.get("education")).byteValue());
        job.setExperience((String) body.get("experience"));
        if (body.get("headcount") != null) job.setHeadcount(((Number) body.get("headcount")).intValue());
        job.setStatus((byte) 1);

        // use JobsMapper.insert which takes JobDTO, so we use the XML insert
        // Actually JobsMapper.insert takes JobDTO. Let me use direct insert approach.
        // The existing insert takes JobDTO, not Job. I need a different approach.
        // Let me construct a JobDTO equivalent
        com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO dto = new com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO();
        dto.setCompanyId(companyId);
        dto.setHrUserId(UserHolder.getUserId());
        dto.setTitle(title);
        dto.setCategory((String) body.get("category"));
        dto.setCity((String) body.get("city"));
        if (body.get("salaryMin") != null) dto.setSalaryMin(((Number) body.get("salaryMin")).intValue());
        if (body.get("salaryMax") != null) dto.setSalaryMax(((Number) body.get("salaryMax")).intValue());
        if (body.get("education") != null) dto.setEducation(((Number) body.get("education")).byteValue());
        dto.setExperience((String) body.get("experience"));
        if (body.get("headcount") != null) dto.setHeadcount(((Number) body.get("headcount")).intValue());
        dto.setStatus((byte) 1);
        dto.setHasCount(((Number) body.get("headcount")).intValue());

        jobsMapper.insert(dto);
        companyMapper.incrementJobCount(companyId, 1);
        clearJobCache();
    }

    @Override
    @Transactional
    public void update(Long id, Map<String, Object> body) {
        Job job = new Job();
        job.setId(id);
        job.setTitle((String) body.get("title"));
        job.setCategory((String) body.get("category"));
        job.setCity((String) body.get("city"));
        if (body.get("salaryMin") != null) job.setSalaryMin(((Number) body.get("salaryMin")).intValue());
        if (body.get("salaryMax") != null) job.setSalaryMax(((Number) body.get("salaryMax")).intValue());
        if (body.get("education") != null) job.setEducation(((Number) body.get("education")).byteValue());
        job.setExperience((String) body.get("experience"));
        if (body.get("headcount") != null) job.setHeadcount(((Number) body.get("headcount")).intValue());

        jobsMapper.updateAdmin(job);
        clearJobCache();
    }

    @OperationLog(action = "AUDIT_JOB", targetType = "job", targetId = "#id")
    @Override
    @Transactional
    public void audit(Long id, Integer status) {
        Job existing = jobsMapper.getJobBy(id);
        if (existing != null && existing.getCompanyId() != null) {
            if (status != null && status == 1) {
                int adjust = existing.getPendingAdjust();
                if (adjust != 0) {
                    companyMapper.incrementJobCount(existing.getCompanyId(), adjust);
                }
            }
        }
        com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO dto = new com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO();
        dto.setId(id);
        dto.setStatus(status.byteValue());
        dto.setPendingAdjust(0);
        jobsMapper.updata(dto);
        clearJobCache();
    }

    @OperationLog(action = "TOGGLE_JOB", targetType = "job", targetId = "#id")
    @Override
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO dto = new com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO();
        dto.setId(id);
        dto.setStatus(status.byteValue());
        jobsMapper.updata(dto);
        clearJobCache();
    }

    @OperationLog(action = "DELETE_JOB", targetType = "job", targetId = "#id")
    @Override
    @Transactional
    public void delete(Long id) {
        jobsMapper.deleteById(id);
        clearJobCache();
    }

    private void clearJobCache() {
        try {
            stringRedisTemplate.delete(stringRedisTemplate.keys(RedisKeyConstants.JOBS_LIST_CACHE_PREFIX + "*"));
            stringRedisTemplate.delete(stringRedisTemplate.keys(RedisKeyConstants.JOBS_HOT_CACHE_PREFIX + "*"));
        } catch (Exception e) {
            log.warn("清除职位缓存失败: {}", e.getMessage());
        }
    }
}
