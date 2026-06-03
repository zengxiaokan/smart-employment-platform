package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminApplicationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;

public interface AdminApplicationService {

    PageInfo<AdminApplicationVO> list(int page, int size, String applicantName, String jobTitle, Integer status);

    ApplicationDitailVo detail(Long id);

    void delete(Long id);
}
