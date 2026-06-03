package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.util.List;

/**
 * 管理员趋势数据 — 近 N 天每日新增用户 + 新增投递
 */
@Data
public class AdminTrendVO {
    private List<TrendItem> trends;

    @Data
    public static class TrendItem {
        private String date;
        private Long newUsers;
        private Long newApplications;
    }
}
