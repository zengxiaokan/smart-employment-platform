package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminTrendVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminRecentUserVO;
import com.itzk.SmartEmploymentPlatform.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminDashboardController")
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public Result<AdminOverviewVO> getOverview() {
        return Result.success(adminDashboardService.getOverview());
    }

    @GetMapping("/trends")
    public Result<AdminTrendVO> getTrends(@RequestParam(defaultValue = "7") int days) {
        return Result.success(adminDashboardService.getTrends(days));
    }

    @GetMapping("/recent-users")
    public Result<List<AdminRecentUserVO>> getRecentUsers(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(adminDashboardService.getRecentUsers(limit));
    }
}
