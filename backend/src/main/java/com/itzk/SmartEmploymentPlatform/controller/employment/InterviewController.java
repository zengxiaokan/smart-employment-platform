package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyTodoMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.CompanyTodo;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.UpdataStatusDTO;
import com.itzk.SmartEmploymentPlatform.service.InterviewService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController("employmentInterviewComtroller")
@Slf4j
@RequestMapping("/user/interviews")
public class InterviewController {


    @Autowired
    private InterviewService interviewService;
    @Autowired
    private InterviewMapper interviewMapper;
    @Autowired
    private CompanyTodoMapper companyTodoMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ApplicationMapper applicationMapper;

    /**
     * 获取关于用户全部面试信息
     * @return
     */
    @GetMapping("/list")
    public Result getInterviewList(){
        Long userId = UserHolder.getUserId();
        if(userId==null || userId == 0){
            return Result.error("用户并没有收到相应的邀请");
        }
        Interview interview = new Interview();
        interview.setUserId(userId);
        return interviewService.getAllInterviewWithConpany(interview);
    }

    /**
     * 用户同意面试申请
     * @return
     */

    @PutMapping("/status")
    public Result updateInterviewStatus(@Valid @RequestBody UpdataStatusDTO updataStatusDTO) {
        Long id = updataStatusDTO.getId();
        Byte status = updataStatusDTO.getStatus();

        Interview view = interviewMapper.getById(id);
        if (view == null) {
            return Result.error("面试记录不存在");
        }
        Long userId = UserHolder.getUserId();
        if (!view.getUserId().equals(userId)) {
            return Result.error("无权操作该面试");
        }

        Interview interview = new Interview();
        interview.setId(id);
        interview.setCandidateRemark(updataStatusDTO.getUserRemark());
        interview.setResponseTime(LocalDateTime.now());

        if (status == Constant.InterviewStatus.ACCEPTED) {
            if (view.getStatus() != Constant.InterviewStatus.PENDING) {
                return Result.success();
            }
            interview.setStatus(Constant.InterviewStatus.ACCEPTED);
            interviewMapper.updata(interview);
            User user = userMapper.getUserById(view.getUserId());
            String realname = user != null && user.getRealname() != null ? user.getRealname() : "未知";
            CompanyTodo todo = new CompanyTodo();
            todo.setCompanyId(view.getCompanyId());
            todo.setContent(realname + "已接受面试邀请");
            todo.setEventTime(view.getInterviewTime());
            todo.setCreatedAt(LocalDateTime.now());
            companyTodoMapper.insert(todo);
            return Result.success();
        }
        if (status == Constant.InterviewStatus.CANCELLED) {
            int cur = view.getStatus();
            // PENDING → 求职者直接拒绝；ACCEPTED → 求职者取消已接受的面试
            if (cur != Constant.InterviewStatus.PENDING && cur != Constant.InterviewStatus.ACCEPTED) {
                return Result.success();
            }
            interview.setStatus(Constant.InterviewStatus.CANCELLED);
            interviewMapper.updata(interview);
            applicationMapper.setStatusById(view.getApplicationId(), Constant.ApplicationStatus.APPLY_CANCER);
            return Result.success();
        }
        return Result.error("状态码错误");
    }


}
