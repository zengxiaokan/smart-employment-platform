package com.itzk.SmartEmploymentPlatform.ai.model;

import lombok.Data;

/**
 * 工作经历 - AI交互模型
 * 日期字段使用String，公司名使用占位符
 */
@Data
public class ExpForAI {

    private Long id;

    /** 公司名称占位符，如 [COMPANY_1] */
    private String company;

    private String position;

    /** 工作开始时间，格式 yyyy-MM-dd */
    private String startTime;

    /** 工作结束时间，格式 yyyy-MM-dd */
    private String endTime;

    /** 工作描述（AI润色目标字段） */
    private String description;
}
