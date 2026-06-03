package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

/**
 * 管理员企业列表项
 */
@Data
public class AdminCompanyVO {
    private Long id;
    private String name;
    private String industry;
    private String size;
    private String city;
    private String address;
    private Integer auditStatus;
    private String phone;
    private Integer financingStage;
    private String officialWeb;
    private String description;
    private String logoUrl;
    private String licenseUrl;
    private String registrant;
    private Integer jobCount;
    private Integer jobConfirm;
    private String createdAt;
}
