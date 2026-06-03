package com.itzk.SmartEmploymentPlatform.ai.model;

import lombok.Data;
import java.util.List;

/**
 * 简历 - AI交互模型
 * 敏感字段使用占位符替换，日期统一为String
 */
@Data
public class ResumeForAI {

    /** 姓名占位符 [NAME] */
    private String name;

    /** 手机号占位符 [PHONE] */
    private String phone;

    /** 邮箱占位符 [EMAIL] */
    private String email;

    private Integer age;

    /** 性别：0-女，1-男 */
    private Byte gender;

    /** 自我评价（AI润色目标字段） */
    private String selfDescription;

    /** 求职意向（AI润色目标字段） */
    private String jobIntention;

    private String city;

    private Integer salaryMin;

    private Integer salaryMax;

    /** 技能标签（AI可格式化大小写/排序） */
    private String skills;

    private String resumeName;

    /** 求职类型：0=不限 1=全职 2=兼职 3=实习 */
    private Integer jobType;

    /** 期望行业 */
    private String industry;

    /** 到岗时间，格式 yyyy-MM-dd HH:mm:ss */
    private String availableFrom;

    private List<EduForAI> educations;

    private List<ExpForAI> experiences;

    private List<ProjForAI> projects;
}
