package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobFavor;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.JobRecommendVO;
import com.itzk.SmartEmploymentPlatform.service.JobsService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;

import java.util.List;

//求职控制器
@Slf4j
@Validated
@RestController("employmentJobsController")
@RequestMapping("/user/jobs")
public class JobsController {

    @Autowired
    private JobsService jobsService;


    @GetMapping("/list")
    public Result<PageResult> queryAllJobs(@Valid JobQueryDTO jobQueryDTO){
        log.info("接收成功{}",jobQueryDTO);
        PageResult pageResult = jobsService.queryAllJobs(jobQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 收藏职位
     * @param jobFavor
     * @return
     */

    @PostMapping("/favorite")
    public Result favoriteJob(@RequestBody JobFavor jobFavor){
        Long userId = UserHolder.getUserId();
        jobFavor.setUserId(userId);
        log.info("收藏职位id:{}",jobFavor.getJobId());

        return jobsService.addJobFavors(jobFavor);
    }

    @DeleteMapping("/favorite/{jobId}")
    public Result deleteJobFavor(@PathVariable Long jobId){
        log.info("取消收藏职位id:{}",jobId);
        return jobsService.deleteFavors(jobId);
    }

    /**
     * 职位浏览
     * @param jobId
     * @return
     */
    @PostMapping("/browse")
    public Result browseJob(@RequestBody Long jobId){
        Long userId = UserHolder.getUserId();
        log.info("id:{}浏览了jobId：{}",userId,jobId);
        return jobsService.addJobBrowse(userId,jobId);
    }

    /**
     * 获取全部收藏职位
     * @return
     */
    @GetMapping("/favorite")
    public Result queryAllFavorJobs(){
        Long userId = UserHolder.getUserId();
        return jobsService.getAllFavorJobs(userId);
    }

    //智能推荐岗位
    @GetMapping("/recommended")
    public Result<List<JobRecommendVO>> recommendedJobs(){
        Long userId = UserHolder.getUserId();
        return jobsService.recommendedJobs(userId);
    }

    /** 热门岗位：按投递数 + 浏览量排序 */
    @GetMapping("/hot")
    public Result<List<Job>> getHotJobs(@Min(1) @RequestParam(defaultValue = "6") int limit){
        return jobsService.getHotJobs(limit);
    }

}
