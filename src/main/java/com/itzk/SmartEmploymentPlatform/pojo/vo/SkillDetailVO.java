package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

/**
 * 技能明细VO
 * 前端渲染 :white_check_mark: Vue  :x: React 效果
 */
@Data
public class SkillDetailVO {

    /** 技能名 */
    private String skillName;
    /** 0=不匹配 1=匹配 */
    private Integer matched;
}
