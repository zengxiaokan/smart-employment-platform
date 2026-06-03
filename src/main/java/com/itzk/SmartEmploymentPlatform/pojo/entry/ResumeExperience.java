package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历工作经历实体类
 * 对应数据库中的简历工作经历表，存储用户的工作经历信息
 */
@Data
public class ResumeExperience {

    /** 工作经历ID */
    private Long id;

    /** 简历ID */
    private Long resumeId;

    /** 公司名称 */
    private String company;

    /** 职位名称 */
    private String position;

    /** 工作开始时间 */
    private LocalDate startTime;

    /** 工作结束时间 */
    private LocalDate endTime;

    /** 工作描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
