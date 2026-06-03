package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsFunnelItem;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsTrendItem;
import com.itzk.SmartEmploymentPlatform.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("recruitmentAnalyticsController")
@Slf4j
@RequestMapping("/hr")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/analytics/overview")
    public Result<AnalyticsOverviewVO> overview(@RequestParam String startDate, @RequestParam String endDate) {
        log.info("数据概览: startDate={}, endDate={}", startDate, endDate);
        AnalyticsOverviewVO vo = analyticsService.overview(startDate, endDate);
        return Result.success(vo);
    }

    @GetMapping("/analytics/trend")
    public Result<List<AnalyticsTrendItem>> trend(@RequestParam String startDate, @RequestParam String endDate) {
        log.info("投递趋势: startDate={}, endDate={}", startDate, endDate);
        List<AnalyticsTrendItem> list = analyticsService.trend(startDate, endDate);
        return Result.success(list);
    }

    @GetMapping("/analytics/funnel")
    public Result<List<AnalyticsFunnelItem>> funnel() {
        List<AnalyticsFunnelItem> list = analyticsService.funnel();
        return Result.success(list);
    }
}
