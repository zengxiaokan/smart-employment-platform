package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobFavor;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.JobRecommendVO;
import jakarta.validation.Valid;

import java.util.List;

public interface JobsService {

    /*
    查询所有职位
     */
    PageResult queryAllJobs(@Valid JobQueryDTO jobQueryDTO);

    //收藏职位
    Result addJobFavors(JobFavor jobFavor);

    Result deleteFavors(Long jobId);

    //浏览职位
    Result addJobBrowse(Long userId, Long jobId);

    //获取全部收藏职位
    Result getAllFavorJobs(Long userId);

    /**
     * 智能推荐岗位
     * @param userId
     * @return
     */
    Result<List<JobRecommendVO>> recommendedJobs(Long userId);

    /**
     * 获取公司全部信息
     * @param jobQueryDTO
     * @return
     */
    PageResult queryAlCompanyJobs(@Valid JobQueryDTO jobQueryDTO);

    //添加职位
    Result addJob(@Valid JobDTO job);

    /** 热门岗位：按投递数+浏览量排序 */
    Result<List<Job>> getHotJobs(int limit);
}
