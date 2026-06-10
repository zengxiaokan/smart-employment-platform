package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.*;
import com.itzk.SmartEmploymentPlatform.pojo.vo.InterviewVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.InterviewService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {


    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private JobsMapper jobsMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @OperationLog(action = "INTERVIEW_INVITE", targetType = "interview", targetId = "#interview.id")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(Interview interview) {
        interviewMapper.insert(interview);
    }

    @Override
    public Result getAllInterviewWithConpany(Interview interview) {
        List<InterviewVo> list = interviewMapper.getAllCompanyInterview(interview);
        return Result.success(list);

    }

    /**
     * 通过申请表查找简历
     * @param applicationId
     * @return
     */
    @Override
    public Result getResumeByApplicationId(Long applicationId) {

        Application a=applicationMapper.getById(applicationId);
        if (a==null){
            return Result.error("用户没有该申请记录");
        }
        Long companyId = UserHolder.getCompanyId();
        if (!a.getCompanyId().equals(companyId)) {
            return Result.error("无权查看该申请");
        }
        Long resumeId = a.getResumeId();
        if (resumeId==null){
            return Result.error("该简历不存在");
        }
        Resume r = resumeMapper.getResumeById(resumeId);
        List <ResumeEducation> educations= resumeMapper.getEducationsByResumeId(resumeId);
        List<ResumeProject> projects = resumeMapper.getProjectsByResumeId(resumeId);
        List<ResumeExperience> experiences = resumeMapper.getExperiencesByResumeId(resumeId);
        ResumeVO vo =new ResumeVO();
        BeanUtils.copyProperties(r,vo);
        vo.setEducations(educations);
        vo.setProjects(projects);
        vo.setExperiences(experiences);


        return Result.success(vo);
    }

    @OperationLog(action = "INTERVIEW_RESULT", targetType = "interview", targetId = "#interview.id",
                  remark = "'status=' + #interview.status")
    @Override
    @Transactional
    public void updateApplyAndView(Interview interview) {
        Integer status = interview.getStatus();
        Long interviewId = interview.getId();
        Long applicationId = interview.getApplicationId();
        String remark = interview.getRemark();

        // 校验公司归属
        Interview existingView = interviewMapper.getById(interviewId);
        if (existingView == null) {
            return;
        }
        Long companyId = UserHolder.getCompanyId();
        if (!existingView.getCompanyId().equals(companyId)) {
            return;
        }

        // 1. 更新面试表状态
        Interview view = new Interview();
        view.setId(interviewId);
        view.setStatus(status);
        view.setRemark(remark);
        interviewMapper.updata(view);

        // 2. 根据面试状态联动申请表
        Application a = new Application();
        a.setId(applicationId);
        a.setHrRemark(remark);

        if (status.equals(Constant.InterviewStatus.PASSED)) {
            // 面试成功 → 申请状态变为"已录用"
            a.setStatus(Constant.ApplicationStatus.Hired);
            applicationMapper.updata(a);
            try { redisTemplate.delete("home:stats"); } catch (Exception ignored) {}
            // 减少岗位剩余招聘人数
            Application app = applicationMapper.getById(applicationId);
            if (app != null && app.getJobId() != null) {
                jobsMapper.decrementHasCount(app.getJobId());
                Job job = jobsMapper.getCompanyIdByjobId(app.getJobId());
                if (job != null && job.getCompanyId() != null) {
                    companyMapper.incrementJobConfirm(job.getCompanyId());
                    if (job.getHasCount() <= 0) {
                        jobsMapper.forceOffline(app.getJobId());
                    }
                }
            }
        } else if (status.equals(Constant.InterviewStatus.REJECTED)) {
            // 拒绝 → 申请状态变为"不合适"
            a.setStatus(Constant.ApplicationStatus.REJECTED);
            applicationMapper.updata(a);
        }
        // 其他状态（待定/过期/取消）不联动申请表
    }

    @Override
    public Result updateInterview(Interview interview) {
        // 校验公司归属
        Interview existing = interviewMapper.getById(interview.getId());
        Long companyId = UserHolder.getCompanyId();
        if (existing == null || !existing.getCompanyId().equals(companyId)) {
            return Result.error("无权操作该面试");
        }
        Integer status = interview.getStatus();
        if (status == null) {
            return Result.error("状态码缺失");
        }
        if (status.equals(Constant.InterviewStatus.PASSED)) {
            return Result.error("用户已通过面试");
        }
        if (status.equals(Constant.InterviewStatus.PENDING)) {
            return Result.error("等待用户确认");
        }
        if (interview.getInterviewTime() != null && interview.getInterviewTime().isBefore(LocalDateTime.now())) {
            return Result.error("面试时间不能早于当前时间");
        }
        interview.setStatus(Constant.InterviewStatus.PENDING);
        interviewMapper.updata(interview);
        if (status > 3) {
            Application a = new Application();
            a.setId(interview.getApplicationId());
            a.setStatus(Constant.ApplicationStatus.INTERVIEW);
            a.setInterviewTime(interview.getInterviewTime());
            a.setAppliedAt(LocalDateTime.now());
            applicationMapper.updata(a);
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelInterview(Interview interview,Long applicationId) {
        // 校验公司归属
        Interview existing = interviewMapper.getById(interview.getId());
        Long companyId = UserHolder.getCompanyId();
        if (existing == null || !existing.getCompanyId().equals(companyId)) {
            return Result.error("无权操作该面试");
        }
        interview.setStatus(Constant.InterviewStatus.CANCELLED);
        interviewMapper.updata(interview);
        //修改职位申请表的状态
        //根据id拿view对象

        Application a = new Application();
        a.setId(interview.getApplicationId());
        a.setStatus(Constant.ApplicationStatus.APPLY_CANCER);
        LocalDateTime now =  LocalDateTime.now();
        a.setUpdatedAt(now);
        a.setId(applicationId);
        applicationMapper.updata(a);

        return Result.success();
    }


}
