package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;

public interface InterviewService {
    /**
     * 建立面试邀约
     * @param interview
     */
    void insert(Interview interview);

    /**
     * 查询全部公司的邀请
     * @param interview
     */
    Result getAllInterviewWithConpany(Interview interview);

    Result getResumeByApplicationId(Long applicationId);

    void updateApplyAndView(Interview interview);

    Result updateInterview(Interview interview);

    //取消面试
    Result cancelInterview(Interview interview,Long applicationId);
}
