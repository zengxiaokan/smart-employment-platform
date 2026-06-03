package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI匹配记录实体
 * 对应 match_records 表，存储一次岗位-简历匹配的评分详情
 */
@Data
public class MatchRecord {

    private Long id;
    /** 岗位ID */
    private Long jobId;
    /** 简历ID */
    private Long resumeId;
    /** 总分（满分100） */
    private BigDecimal totalScore;
    /** 技能分（满分60） */
    private BigDecimal skillScore;
    /** 经验分（满分20） */
    private BigDecimal experienceScore;
    /** 学历分（满分10） */
    private BigDecimal educationScore;
    /** 薪资分（满分10） */
    private BigDecimal salaryScore;
    /** AI推荐语，懒加载生成 */
    private String matchSummary;
    /** 0=待查看 1=已查看 2=已邀请 */
    private Integer status;
    /** 匹配时间 */
    private LocalDateTime createdAt;
}
