package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminCompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;

import java.util.Map;

public interface AdminCompanyService {

    PageInfo<AdminCompanyVO> list(int page, int size, String name, String industry, Integer auditStatus);

    AdminCompanyVO detail(Long id);

    AdminUserVO getRegistrant(Long companyId);

    void create(Map<String, Object> body);

    void update(Long id, Map<String, Object> body);

    void audit(Long id, Integer auditStatus);

    void delete(Long id);
}
