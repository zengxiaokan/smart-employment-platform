package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminOverviewVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminRecentUserVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminTrendVO;
import com.itzk.SmartEmploymentPlatform.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private JobsMapper jobsMapper;
    @Autowired
    private ApplicationMapper applicationMapper;

    @Override
    public AdminOverviewVO getOverview() {
        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setJobSeekerCount(userMapper.countJobSeekers());
        vo.setCompanyCount(companyMapper.countAudited());
        vo.setActiveJobCount(jobsMapper.countAllActive());
        vo.setTotalApplicationCount(jobsMapper.sumApplyCount());
        vo.setMonthApplicationCount(applicationMapper.countByCurrentMonth());

        // 投递量 TOP 10 职位排行
        List<Map<String, Object>> topJobs = jobsMapper.getTopJobsByApplyCount(10);
        List<AdminOverviewVO.JobAppRank> ranking = new ArrayList<>();
        for (Map<String, Object> row : topJobs) {
            AdminOverviewVO.JobAppRank item = new AdminOverviewVO.JobAppRank();
            item.setJobId(((Number) row.get("jobId")).longValue());
            item.setJobTitle((String) row.get("jobTitle"));
            item.setCompanyName((String) row.get("companyName"));
            item.setApplyCount(((Number) row.get("applyCount")).longValue());
            ranking.add(item);
        }
        vo.setJobAppRanking(ranking);

        List<Map<String, Object>> roleRows = userMapper.countByRole();
        long total = roleRows.stream().mapToLong(r -> ((Number) r.get("count")).longValue()).sum();
        List<AdminOverviewVO.RoleDistribution> distributions = new ArrayList<>();
        for (Map<String, Object> row : roleRows) {
            Integer role = ((Number) row.get("role")).intValue();
            Long count = ((Number) row.get("count")).longValue();
            AdminOverviewVO.RoleDistribution dist = new AdminOverviewVO.RoleDistribution();
            dist.setRole(role);
            dist.setCount(count);
            dist.setPercent(total > 0 ? (int) Math.round(count * 100.0 / total) : 0);
            distributions.add(dist);
        }
        vo.setRoleDistribution(distributions);
        return vo;
    }

    @Override
    public AdminTrendVO getTrends(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days - 1).atStartOfDay();

        List<Map<String, Object>> userRows = userMapper.countNewUsersByDate(since);
        Map<String, Long> userMap = new LinkedHashMap<>();
        for (Map<String, Object> row : userRows) {
            userMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }

        List<Map<String, Object>> appRows = applicationMapper.countNewByDate(since);
        Map<String, Long> appMap = new LinkedHashMap<>();
        for (Map<String, Object> row : appRows) {
            appMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }

        List<AdminTrendVO.TrendItem> trends = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String date = LocalDate.now().minusDays(days - 1 - i).toString();
            AdminTrendVO.TrendItem item = new AdminTrendVO.TrendItem();
            item.setDate(date);
            item.setNewUsers(userMap.getOrDefault(date, 0L));
            item.setNewApplications(appMap.getOrDefault(date, 0L));
            trends.add(item);
        }

        AdminTrendVO vo = new AdminTrendVO();
        vo.setTrends(trends);
        return vo;
    }

    @Override
    public List<AdminRecentUserVO> getRecentUsers(int limit) {
        List<Map<String, Object>> rows = userMapper.getRecentUsers(limit);
        return rows.stream().map(row -> {
            AdminRecentUserVO vo = new AdminRecentUserVO();
            vo.setId(((Number) row.get("id")).longValue());
            vo.setUsername((String) row.get("username"));
            vo.setNickname((String) row.get("nickname"));
            vo.setPhone((String) row.get("phone"));
            vo.setRole(((Number) row.get("role")).intValue());
            vo.setCreatedAt(row.get("created_at") instanceof LocalDateTime
                    ? (LocalDateTime) row.get("created_at")
                    : LocalDateTime.parse(row.get("created_at").toString().replace(" ", "T")));
            return vo;
        }).collect(Collectors.toList());
    }
}
