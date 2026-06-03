package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.UpdataStatusDTO;
import com.itzk.SmartEmploymentPlatform.service.InterviewService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("recruitmenttInterviewController")
@Slf4j
@RequestMapping("/hr/interviews")
public class InterviewController {


    @Autowired
    private InterviewService interviewService;

    @Autowired
    private InterviewMapper interviewMapper;

    /**
     * 获取关于公司全部面试信息
     * @return
     */
    @GetMapping("/list")
    public Result getInterviewList(){
        Long companyId = UserHolder.getCompanyId();
        if(companyId==null || companyId == 0){
            return Result.error("用户并没有相应的公司");
        }
        Interview interview = new Interview();
        interview.setCompanyId(companyId);
        return interviewService.getAllInterviewWithConpany(interview);
    }

    /**
     * 通过申请id查询简历
     * @param applicationId
     * @return
     */
    @GetMapping("/resume/{applicationId}")
    public Result resumeInterview(@PathVariable Long applicationId){
            if (applicationId == null){
                return Result.error("参数传递错误");
            }
            return interviewService.getResumeByApplicationId(applicationId);
    }

    /**
     * 更新面试状态
     * 当面试状态变为"面试成功"(3)时，自动将申请状态改为"已录用"(5)
     */
    @PutMapping("/status")
    public Result updateInterviewStatus(@RequestBody UpdataStatusDTO dto) {
        log.info("更新面试状态: interviewId={}, status={}", dto.getId(), dto.getStatus());
        Long id = dto.getId();
        Byte status = dto.getStatus();

        Interview view = interviewMapper.getById(id);
        // 允许：待确认(0)、已接受(1)、待定(2)、已过期(5)
        // 禁止：面试成功(3)、拒绝(4)、已取消(6) —— 这些是终态
        int s = view.getStatus();
        if (s == Constant.InterviewStatus.PASSED
                || s == Constant.InterviewStatus.REJECTED
                || s == Constant.InterviewStatus.CANCELLED) {
            return Result.error("当前状态不允许修改");
        }

        Interview interview = new Interview();
        interview.setId(id);
        interview.setStatus((int) status);
        interview.setApplicationId(view.getApplicationId());
        interview.setRemark(dto.getHrRemark());
        interviewService.updateApplyAndView(interview);
        return Result.success();
    }


    @GetMapping("/{id}")
    public Result getInterviewDetail(@PathVariable Long id){
        Interview interview = interviewMapper.getById(id);
        return Result.success(interview);
    }

    @GetMapping("query")
    public Result getViewByUserIDAndJobId(Long userId,Long jobId){

        Interview interview = interviewMapper.getByUserIDAndJobId(userId,jobId);
        return Result.success(interview);
    }


}
