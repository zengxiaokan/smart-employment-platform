package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class AdminOverviewVO {
    private Long jobSeekerCount;
    private Long companyCount;
    private Long activeJobCount;
    private Long totalApplicationCount;       // 累计投递量（所有岗位 apply_count 之和）
    private Long monthApplicationCount;       // 本月新增投递事件数
    private List<RoleDistribution> roleDistribution;
    private List<JobAppRank> jobAppRanking;   // 投递量 TOP 职位

    @Data
    public static class RoleDistribution {
        private Integer role;
        private Long count;
        private Integer percent;
    }

    @Data
    public static class JobAppRank {
        private Long jobId;
        private String jobTitle;
        private String companyName;
        private Long applyCount;
    }
}
