package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobDTO {

    private Long id;


    private Long companyId;

    // HR用户ID（必须传）

    private Long hrUserId;

    // 职位名称（必须传，长度1-100）
    private String title;


    private String category;


    private Integer salaryMin;


    private Integer salaryMax;


    private String city;


    private String address;


    private String experience;


    private Byte education;


    private String tags;

    //技能
    private String jobSkills;


    private Integer headcount;

    // 岗位职责
    private String dutyContent;

    // 任职要求
    private String requireContent;

    // 福利
    private String benefits;


    private Byte status;


    private Integer viewCount;


    private Integer applyCount;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    //剩余岗位
    private int hasCount;

    //HR调整岗位数（正=增加，负=减少），仅在编辑时使用
    private Integer delta;

    //待审核岗位调整数
    private Integer pendingAdjust;
}
