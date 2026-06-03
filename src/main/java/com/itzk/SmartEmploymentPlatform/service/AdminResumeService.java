package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminResumeVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;

public interface AdminResumeService {

    PageInfo<AdminResumeVO> list(int page, int size, String name, String jobIntention);

    ResumeVO detail(Long id);

    void delete(Long id);
}
