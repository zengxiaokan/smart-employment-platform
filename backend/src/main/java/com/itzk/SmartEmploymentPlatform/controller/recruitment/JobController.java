package com.itzk.SmartEmploymentPlatform.controller.recruitment;


import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobQueryDTO;
import com.itzk.SmartEmploymentPlatform.service.JobsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("recruitmentJobController")
@Slf4j
@RequestMapping("/hr")
public class JobController {

    @Autowired
    private JobsService jobsService;

    @GetMapping("/jobs")
    public Result<PageResult> getCompanyJobs(@Valid JobQueryDTO jobQueryDTO){

        log.info("接收成功{}",jobQueryDTO);
        PageResult pageResult = jobsService.queryAlCompanyJobs(jobQueryDTO);
        return Result.success(pageResult);

    }

    @PostMapping("/jobs")
    public Result addJob(@Valid @RequestBody JobDTO jobDTO){
        return jobsService.addJob(jobDTO);
    }


}
