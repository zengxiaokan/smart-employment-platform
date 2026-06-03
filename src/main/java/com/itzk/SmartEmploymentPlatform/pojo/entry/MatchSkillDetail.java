package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;

/**
 * 技能匹配明细实体
 * 对应 match_skill_details 表，逐技能记录匹配/不匹配
 */
@Data
public class MatchSkillDetail {

    private Long id;
    /** 关联 match_records.id */
    private Long matchId;
    /** 技能名称 */
    private String skillName;
    /** 0=不匹配 1=匹配 */
    private Integer matched;
}
