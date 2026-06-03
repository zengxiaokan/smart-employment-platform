package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminRecentUserVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminTrendVO;

import java.util.List;

/**
 * 管理员首页概览服务
 */
public interface AdminDashboardService {

    AdminOverviewVO getOverview();

    AdminTrendVO getTrends(int days);

    List<AdminRecentUserVO> getRecentUsers(int limit);
}
