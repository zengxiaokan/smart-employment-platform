package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历项目经验实体类
 * 对应数据库中的简历项目经验表，存储用户的项目经历信息
 */
@Data
public class ResumeProject {

    /** 项目经验ID */
    private Long id;

    /** 简历ID */
    private Long resumeId;

    /** 项目名称 */
    private String name;

    /** 担任角色 */
    private String role;

    /** 项目开始时间 */
    private LocalDate startTime;

    /** 项目结束时间 */
    private LocalDate endTime;

    /** 项目描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
