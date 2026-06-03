package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.FavorResumDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryResumeDTO;

public interface SearchGoodPeopleService {
    //获取所有求职意向
    Result getAllIntention(QueryResumeDTO dto);

    //根据id获取简历
    Result getResumeById(Long resumeId);

    //公司收藏人才
    Result saveCompanyResume(FavorResumDTO dto);

    //取消收藏
    Result cancelCompanyResume(Long resumeId);

    //获取全部公司收藏对象

    Result getFavorites();

    //修改备注
    Result updataRemark(Long resumeId, String reason);
}
