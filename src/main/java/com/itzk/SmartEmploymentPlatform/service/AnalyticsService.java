package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsFunnelItem;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsTrendItem;

import java.util.List;

public interface AnalyticsService {

    AnalyticsOverviewVO overview(String startDate, String endDate);

    List<AnalyticsTrendItem> trend(String startDate, String endDate);

    List<AnalyticsFunnelItem> funnel();
}
