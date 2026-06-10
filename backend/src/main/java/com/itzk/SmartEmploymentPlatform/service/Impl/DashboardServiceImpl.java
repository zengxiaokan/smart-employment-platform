package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyTodoMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.DashboardVO;
import com.itzk.SmartEmploymentPlatform.service.DashboardService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private JobsMapper jobsMapper;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private InterviewMapper interviewMapper;
    @Autowired
    private CompanyTodoMapper companyTodoMapper;

    @Override
    public DashboardVO getDashboard(Long companyId) {
        Long currentCompanyId = UserHolder.getCompanyId();
        if (currentCompanyId == null || !currentCompanyId.equals(companyId)) {
            throw new RuntimeException("无权查看该企业数据");
        }
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        DashboardVO vo = new DashboardVO();
        vo.setJobCount(jobsMapper.countActiveByCompanyId(companyId));
        vo.setResumeCount(jobsMapper.sumApplyCountByCompanyId(companyId).intValue());
        vo.setInterviewCount(interviewMapper.countByCompanyIdAndMonth(companyId, monthStart));
        vo.setHireCount(applicationMapper.countHiredByCompanyIdAndMonth(companyId, monthStart));

        // 最新5条投递
        ApplicationQueryDTO query = new ApplicationQueryDTO();
        query.setCompanyId(companyId);
        query.setPage(1);
        query.setSize(5);
        PageHelper.startPage(query.getPage(), query.getSize());
        Page<ApplicationHrVO> page = (Page<ApplicationHrVO>) applicationMapper.getCompanyApply(query);
        vo.setLatestApplications(page.getResult());

        // 最近5条待办
        vo.setTodoList(companyTodoMapper.getLatestByCompanyId(companyId));

        return vo;
    }
}
