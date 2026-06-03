package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.mapper.AnalyticsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsFunnelItem;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsTrendItem;
import com.itzk.SmartEmploymentPlatform.service.AnalyticsService;
import com.itzk.SmartEmploymentPlatform.utils.RedisKeyConstants;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsMapper analyticsMapper;
    private final CompanyMapper companyMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public AnalyticsOverviewVO overview(String startDate, String endDate) {
        Long companyId = UserHolder.getCompanyId();
        String cacheKey = RedisKeyConstants.ANALYTICS_OVERVIEW_PREFIX + companyId + ":" + startDate + ":" + endDate;

        // 先查缓存，Redis 不可用时降级查 DB
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return JSON.parseObject(cached, AnalyticsOverviewVO.class);
            }
        } catch (Exception e) {
            log.warn("Redis 读取分析概览缓存失败，降级查 DB: {}", e.getMessage());
        }

        String end = endDate + " 23:59:59";

        Company company = companyMapper.getById(companyId);
        int totalJobs = company != null && company.getJobCount() != null ? company.getJobCount() : 0;
        int activeJobs = analyticsMapper.countActiveJobs(companyId);

        // 顶部概览卡片 — 全量累计数据，不受日期筛选影响
        int totalApplications = analyticsMapper.countApplicationsAll(companyId);
        int interviewed = analyticsMapper.countInterviewedAll(companyId);
        int hired = analyticsMapper.countHiredAll(companyId);

        // 最近一周投递数
        LocalDate weekStartDate = LocalDate.parse(endDate).minusDays(7);
        if (weekStartDate.isBefore(LocalDate.parse(startDate))) {
            weekStartDate = LocalDate.parse(startDate);
        }
        String weekStart = weekStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00:00";
        int weekApplications = analyticsMapper.countApplications(companyId, weekStart, end);

        double interviewRate = totalApplications > 0
                ? Math.round((double) interviewed / totalApplications * 10000.0) / 100.0
                : 0.0;
        double hireRate = totalApplications > 0
                ? Math.round((double) hired / totalApplications * 10000.0) / 100.0
                : 0.0;

        AnalyticsOverviewVO vo = new AnalyticsOverviewVO();
        vo.setTotalJobs(totalJobs);
        vo.setActiveJobs(activeJobs);
        vo.setTotalApplications(totalApplications);
        vo.setWeekApplications(weekApplications);
        vo.setInterviewRate(interviewRate);
        vo.setHireRate(hireRate);

        // 写入缓存，Redis 不可用时忽略
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(vo), 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入分析概览缓存失败: {}", e.getMessage());
        }
        return vo;
    }

    @Override
    public List<AnalyticsTrendItem> trend(String startDate, String endDate) {
        Long companyId = UserHolder.getCompanyId();
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";

        Map<String, Integer> appMap = analyticsMapper.dailyApplications(companyId, start, end)
                .stream()
                .collect(Collectors.toMap(
                        m -> m.get("dt").toString(),
                        m -> ((Number) m.get("cnt")).intValue()
                ));
        Map<String, Integer> interviewMap = analyticsMapper.dailyInterviews(companyId, start, end)
                .stream()
                .collect(Collectors.toMap(
                        m -> m.get("dt").toString(),
                        m -> ((Number) m.get("cnt")).intValue()
                ));

        List<AnalyticsTrendItem> result = new ArrayList<>();
        LocalDate startDt = LocalDate.parse(startDate);
        LocalDate endDt = LocalDate.parse(endDate);
        long days = ChronoUnit.DAYS.between(startDt, endDt) + 1;

        for (int i = 0; i < days; i++) {
            String date = startDt.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            AnalyticsTrendItem item = new AnalyticsTrendItem();
            item.setDate(date);
            item.setApplications(appMap.getOrDefault(date, 0));
            item.setInterviews(interviewMap.getOrDefault(date, 0));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<AnalyticsFunnelItem> funnel() {
        Long companyId = UserHolder.getCompanyId();
        return analyticsMapper.funnel(companyId);
    }
}
