package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历教育经历实体类
 * 对应数据库中的简历教育经历表，存储用户的教育背景信息
 */
@Data
public class ResumeEducation {

    /** 教育经历ID */
    private Long id;

    /** 简历ID */
    private Long resumeId;

    /** 学校名称 */
    private String school;

    /** 专业名称 */
    private String major;

    /** 学历：0-初中，1-高中，2-中专，3-大专，4-本科，5-硕士，6-博士 */
    private Byte education;

    //获得奖励及任职情况
    private String description;

    /** 入学时间 */
    private LocalDate startTime;

    /** 毕业时间 */
    private LocalDate endTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
