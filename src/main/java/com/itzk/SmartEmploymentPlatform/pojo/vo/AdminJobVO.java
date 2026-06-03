package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

/**
 * 管理员职位列表项 — 包含职位全部字段
 */
@Data
public class AdminJobVO {
    private Long id;
    private String title;
    private Long companyId;
    private String companyName;
    private String category;
    private Integer salaryMin;
    private Integer salaryMax;
    private String city;
    private String address;
    private String experience;
    private Integer education;
    private String tags;
    private String jobSkills;
    private Integer headcount;
    private Integer hasCount;
    private Integer pendingAdjust;
    private String dutyContent;
    private String requireContent;
    private String benefits;
    private Integer status;
    private Integer viewCount;
    private Integer applyCount;
    private String createdAt;
    private String updatedAt;
}
