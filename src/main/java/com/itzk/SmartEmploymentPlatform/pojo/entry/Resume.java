package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简历实体类
 * 对应数据库中的简历表，存储用户的基本简历信息
 */
@Data
public class Resume {

    /** 简历ID */
    private Long id;

    /**简历名字*/
    private String resumeName;

    /** 用户ID */
    private Long userId;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 年龄 */
    private Integer age;

    /** 性别：0-女，1-男 */
    private Byte gender;

    private String skills;

    /** 自我评价 */
    private String selfDescription;

    /** 求职意向 */
    private String jobIntention;

    /** 期望工作城市 */
    private String city;

    /** 期望最低薪资 */
    private Integer salaryMin;

    /** 期望最高薪资 */
    private Integer salaryMax;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    //人物头像
    private String characterAvatar;

    //是否默认
    private byte isDefault;

    /** 求职类型：0=不限 1=全职 2=兼职 3=实习 */
    private Integer jobType;

    /** 期望行业 */
    private String industry;

    /** 到岗时间 */
    private String availableFrom;

    /** 毕业院校 */
    private String graduationSchool;

    /** 总工作年限（冗余字段，避免联表查履历） */
    private Integer totalWorkYears;

    /** 最高学历（冗余字段，0-6对应Education枚举） */
    private Byte maxEducation;


    //hr备注参数
    private String favoriteReason;
}
