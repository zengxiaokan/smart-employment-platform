package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class OperationLogVO {
    private Long id;
    private Long userId;
    private String username;
    private String operatorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String ipAddress;
    private String remark;
    private String createdAt;
}
