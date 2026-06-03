package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;

import java.util.Map;

/**
 * 管理员用户管理服务 — 求职者 + 管理员 + HR
 */
public interface AdminUserService {

    PageInfo<AdminUserVO> listUsers(int page, int size, String username, String keyword, Integer role, Integer status);

    void createUser(Map<String, Object> body);

    void updateUser(Long id, Map<String, Object> body);

    void deleteUser(Long id);

    PageInfo<AdminHrVO> listHrs(int page, int size, String name, Integer auditStatus, Integer status);

    void createHr(Map<String, Object> body);

    void updateHr(Long id, Map<String, Object> body);

    void deleteHr(Long id);
}
