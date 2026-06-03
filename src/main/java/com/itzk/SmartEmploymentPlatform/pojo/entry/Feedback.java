package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;

@Data
public class Feedback {
    private Long id;
    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private String images;
    private Integer status;
    private String reply;
    private String repliedAt;
    private String createdAt;
    private String updatedAt;
    private Integer targetType;
    private Long targetId;
}
