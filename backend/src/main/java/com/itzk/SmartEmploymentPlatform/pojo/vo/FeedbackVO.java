package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class FeedbackVO {
    private Long id;
    private Long userId;
    private String username;
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
