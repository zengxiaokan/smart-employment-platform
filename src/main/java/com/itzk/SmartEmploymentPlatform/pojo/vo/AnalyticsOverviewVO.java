package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class AnalyticsOverviewVO {
    private int totalJobs;
    private int activeJobs;
    private int totalApplications;
    private int weekApplications;
    private double interviewRate;
    private double hireRate;
}
