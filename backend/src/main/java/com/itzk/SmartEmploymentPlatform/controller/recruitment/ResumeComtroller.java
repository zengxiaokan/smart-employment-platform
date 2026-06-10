package com.itzk.SmartEmploymentPlatform.controller.recruitment;


import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyTodoMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Application;
import com.itzk.SmartEmploymentPlatform.pojo.entry.CompanyTodo;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.UpdataStatusDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.service.InterviewService;
import com.itzk.SmartEmploymentPlatform.service.ResumeService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

@RestController("recruitmentResumeComtroller")
@Slf4j
@RequestMapping("/hr/resumes")
public class ResumeComtroller {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private InterviewService interviewService;
    @Autowired
    private CompanyTodoMapper companyTodoMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 查询公司所有职位申请
     * @return
     */
    @GetMapping("/list")
    public Result CompanyResume(ApplicationQueryDTO applicationQueryDTO) {
        Long companyId = UserHolder.getCompanyId();
        applicationQueryDTO.setCompanyId(companyId);
        return resumeService.getCompanyApply(applicationQueryDTO);

    }

    @GetMapping("/detail/{id}")
    public Result<ResumeVO> getResumeDetail(@PathVariable Long id,@RequestParam Long applicationId) {
        log.info("查询简历详情: id={}", id);
        ResumeVO vo = resumeService.getResumeDetail(id);
        if (vo == null) {
            return Result.error("简历不存在");
        }


        return resumeService.getResumeDetail(id,applicationId);

    }

    /**
     * 建立面试邀约
     * @param interview
     * @return
     */
    @PostMapping("/interview")
    public Result createInterview(@Valid @RequestBody Interview interview){
        LocalDateTime now = LocalDateTime.now();
        if (interview.getId() == null){
            log.info("建立面试邀约{}", interview);
            Long hrUserId = UserHolder.getUserId();
            Long companyId = UserHolder.getCompanyId();
            interview.setHrUserId(hrUserId);
            interview.setCompanyId(companyId);
            byte status = Constant.ApplicationStatus.INTERVIEW;

            interview.setStatus(Constant.InterviewStatus.PENDING);
            now = LocalDateTime.now();
            interview.setCreatedAt(now);
            interview.setUpdatedAt(now);

            Result result = resumeService.createInterview(interview, status);
            // 插入公司待办：安排面试
            User user = userMapper.getUserById(interview.getUserId());
            String realname = user != null && user.getRealname() != null ? user.getRealname() : "未知";
            CompanyTodo todo = new CompanyTodo();
            todo.setCompanyId(companyId);
            todo.setContent("已安排与" + realname + "的面试");
            todo.setEventTime(interview.getInterviewTime());
            todo.setCreatedAt(now);
            companyTodoMapper.insert(todo);
            return result;
        }else {
            interview.setUpdatedAt(now);
            return interviewService.updateInterview(interview);
        }

    }

    @PutMapping("status")
    public Result updateStatus(@Valid @RequestBody UpdataStatusDTO updataStatusDTO){
        log.info("id={}的状态要修改为:{}", updataStatusDTO.getId(), updataStatusDTO.getStatus());
        Long companyId = UserHolder.getCompanyId();
        Application app = applicationMapper.getById(updataStatusDTO.getId());
        if (app == null || !app.getCompanyId().equals(companyId)) {
            return Result.error("无权操作该申请");
        }
        applicationMapper.setStatusById(updataStatusDTO.getId(),updataStatusDTO.getStatus());
        return Result.success();
    }


    @PutMapping("remark")
    public Result updateRemark(@Valid @RequestBody UpdataStatusDTO updataStatusDTO){
        log.info("id={}的备注要修改为:{}", updataStatusDTO.getId(), updataStatusDTO.getHrRemark());
        Long companyId = UserHolder.getCompanyId();
        Application app = applicationMapper.getById(updataStatusDTO.getId());
        if (app == null || !app.getCompanyId().equals(companyId)) {
            return Result.error("无权操作该申请");
        }
        Application a = new Application();
        a.setId(updataStatusDTO.getId());
        a.setHrRemark(updataStatusDTO.getHrRemark());

        applicationMapper.updata(a);
        return Result.success();
    }






}
