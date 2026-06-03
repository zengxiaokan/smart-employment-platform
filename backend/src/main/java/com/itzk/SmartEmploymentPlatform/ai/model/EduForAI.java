package com.itzk.SmartEmploymentPlatform.ai.model;

import lombok.Data;

/**
 * 教育经历 - AI交互模型
 * 日期字段使用String，学校名使用占位符
 */
@Data
public class EduForAI {

    private Long id;

    /** 学校名称占位符，如 [SCHOOL_1] */
    private String school;

    private String major;

    /** 学历：0-初中，1-高中，2-中专，3-大专，4-本科，5-硕士，6-博士 */
    private Byte education;

    /** 获得奖励及任职情况（AI润色目标字段） */
    private String description;

    /** 入学时间，格式 yyyy-MM-dd */
    private String startTime;

    /** 毕业时间，格式 yyyy-MM-dd */
    private String endTime;
}
