package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.DashboardVO;
import com.itzk.SmartEmploymentPlatform.service.DashboardService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页概览
 */
@Slf4j
@RestController
@RequestMapping("/hr/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getDashboard() {
        Long companyId = UserHolder.getCompanyId();
        log.info("查询公司首页概览: companyId={}", companyId);
        DashboardVO vo = dashboardService.getDashboard(companyId);
        return Result.success(vo);
    }
}
