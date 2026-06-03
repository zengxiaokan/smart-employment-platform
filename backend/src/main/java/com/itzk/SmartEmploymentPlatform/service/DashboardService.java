package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.vo.DashboardVO;

/**
 * 首页概览服务
 */
public interface DashboardService {

    /** 获取公司首页概览数据 */
    DashboardVO getDashboard(Long companyId);
}
