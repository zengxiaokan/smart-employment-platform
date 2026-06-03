package com.itzk.SmartEmploymentPlatform.ai.model;

import lombok.Data;

/**
 * 项目经验 - AI交互模型
 * 日期字段使用String
 */
@Data
public class ProjForAI {

    private Long id;

    private String name;

    private String role;

    /** 项目开始时间，格式 yyyy-MM-dd */
    private String startTime;

    /** 项目结束时间，格式 yyyy-MM-dd */
    private String endTime;

    /** 项目描述（AI润色目标字段） */
    private String description;
}
