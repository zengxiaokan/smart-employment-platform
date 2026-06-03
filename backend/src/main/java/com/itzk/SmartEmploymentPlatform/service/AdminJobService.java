package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminJobVO;

import java.util.Map;

public interface AdminJobService {

    PageInfo<AdminJobVO> list(int page, int size, String title, String companyName, Integer status);

    void create(Map<String, Object> body);

    void update(Long id, Map<String, Object> body);

    void audit(Long id, Integer status);

    void toggleStatus(Long id, Integer status);

    void delete(Long id);
}
