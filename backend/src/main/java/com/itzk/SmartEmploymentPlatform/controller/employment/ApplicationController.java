package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.itzk.SmartEmploymentPlatform.pojo.Result;

import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleApplicationVo;
import com.itzk.SmartEmploymentPlatform.service.ApplicationService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user")
public class ApplicationController {


    @Autowired
    private ApplicationService applicationService;


    /**
     * 岗位投递
     * @param application
     * @return
     */
    @PostMapping("/job/apply")
    public Result commitApplication(@RequestBody ApplicationDTO application) {
        log.info("职位申请 : {}", application);

        Long userId = UserHolder.getUserId();
        application.setUserId(userId);

        return applicationService.commitApplication(application);


    }

    /**
     * 返回职位列表
     * @return
     */

    @GetMapping("/applications")
    public Result<List<SimpleApplicationVo>> getApplications() {

        Long userId = UserHolder.getUserId();
        log.info("根据id查询职位申请id:{}",userId);
        List<SimpleApplicationVo> list= applicationService.getByUserId(userId);
        log.info("查到的数据为{}",list.toString());

        return Result.success(list);
    }

    //不想投递该公司
    @PutMapping("/application/cancel/{applicationId}")
    public Result cancelApplication(@PathVariable("applicationId") Long applicationId) {
        Long userId = UserHolder.getUserId();
        log.info("取消申请的是：{}",userId);
        if (userId==null){
            return Result.error("用户未登录");
        }
        return applicationService.deleteByUSerIdAndId(userId,applicationId);

    }

    //想通了再次投递该公司
    @PutMapping("/application/reapply/{applicationId}")
    public Result reapplyApplication(@PathVariable("applicationId") Long applicationId) {
        Long userId = UserHolder.getUserId();
        log.info("再次投递的是:{}",userId);
        if (userId==null){
            return Result.error("用户未登录");
        }
        return applicationService.replyApplication(userId,applicationId);
    }

    @GetMapping("/application/detail/{applicationId}")
    public Result<ApplicationDitailVo> getApplicationDetail(@PathVariable("applicationId") Long applicationId) {

        return applicationService.JobandConpanyInfo(applicationId);
    }




}
