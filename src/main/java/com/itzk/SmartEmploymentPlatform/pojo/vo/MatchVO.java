package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 匹配列表VO
 * 一条JOIN SQL联表match_records+resumes查出，前端卡片直接渲染
 */
@Data
public class MatchVO {

    /** 投递申请ID */
    private Long applicationId;
    /** 匹配记录ID（未评分时为null，前端据此判断是否已评分） */
    private Long matchId;
    private Long resumeId;
    /** 用户ID（联表resumes.user_id，用于发送面试邀约） */
    private Long userId;
    /** 姓名（联表resumes.name） */
    private String name;
    /** 头像URL（联表resumes.character_avatar） */
    private String avatar;
    /** 最高学历，文字如"本科"（Java层计算） */
    private String education;
    /** 总分（满分100，未评分时为null） */
    private BigDecimal totalScore;
    /** 技能分 */
    private BigDecimal skillScore;
    /** 经验分 */
    private BigDecimal experienceScore;
    /** 学历分 */
    private BigDecimal educationScore;
    /** 薪资分 */
    private BigDecimal salaryScore;
    /** AI推荐语 */
    private String matchSummary;
    /** 最高学历值（冗余字段，0-6，从resumes表直接获取，避免联表） */
    private Byte maxEducation;
    /** 已匹配技能摘要，如"Vue、React" */
    private String matchedSkills;
    /** 0=待查看 1=已查看 2=已邀请（未评分时为null） */
    private Integer status;
}
