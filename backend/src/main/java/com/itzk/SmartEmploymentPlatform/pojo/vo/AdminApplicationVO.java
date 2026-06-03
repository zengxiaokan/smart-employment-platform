package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class AdminApplicationVO {
    private Long id;
    private String applicantName;
    private String jobTitle;
    private String companyName;
    private Integer status;
    private String appliedAt;
    private String updatedAt;
}
