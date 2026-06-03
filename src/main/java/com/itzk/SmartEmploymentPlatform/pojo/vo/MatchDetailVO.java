package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 匹配详情VO
 * 包含完整评分、AI推荐语、技能逐项明细
 */
@Data
public class MatchDetailVO {

    private Long matchId;
    private Long jobId;
    /** 岗位名称 */
    private String jobTitle;
    private Long resumeId;
    /** 候选人姓名 */
    private String name;
    /** 头像URL */
    private String avatar;
    /** 最高学历文字 */
    private String education;
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
    /** AI推荐语（首次查看时懒加载生成） */
    private String matchSummary;
    /** 0=待查看 1=已查看 2=已邀请 */
    private Integer status;
    /** 技能匹配明细列表 */
    private List<SkillDetailVO> skillDetails;
}
