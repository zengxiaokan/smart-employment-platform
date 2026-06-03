package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleApplicationVo;

import java.util.List;

public interface ApplicationService {


    Result deleteByUSerIdAndId(long userId, Long applicationId);


    /**
     * 提交简历
     * @param application
     * @return
     */
    Result commitApplication(ApplicationDTO application);

    /**
     * 通过用户id查询岗位申请表
     * @param userId
     * @return
     */
    List<SimpleApplicationVo> getByUserId(Long userId);

    //想通了想再投一次
    Result replyApplication(Long userId, Long applicationId);

    //查询简历详情
    Result<ApplicationDitailVo> JobandConpanyInfo(Long applicationId);
}
