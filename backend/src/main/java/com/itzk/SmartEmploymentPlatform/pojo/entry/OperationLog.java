package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String operatorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String ipAddress;
    private String remark;
    private LocalDateTime createdAt;
}
