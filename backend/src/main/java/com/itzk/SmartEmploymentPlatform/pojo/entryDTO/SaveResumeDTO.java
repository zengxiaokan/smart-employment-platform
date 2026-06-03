package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeEducation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeExperience;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeProject;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 保存简历的请求参数DTO
 * 用于创建或更新简历时接收前端提交的简历数据
 */
@Data
public class SaveResumeDTO {

    /** 简历ID（更新时需要传入） */
    private Long id;

    /** 用户ID */
    private Long userId;

    private String resumeName;

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

    //用户技能标签
    private String skills;

    /** 教育经历列表 */
    private List<ResumeEducation> educations;

    /** 工作经历列表 */
    private List<ResumeExperience> experiences;

    /** 项目经验列表 */
    private List<ResumeProject> projects;

    //照片
    private String characterAvatar;

    /** 求职类型：0=不限 1=全职 2=兼职 3=实习 */
    private Integer jobType;

    /** 期望行业 */
    private String industry;

    /** 到岗时间 */
    private String availableFrom;

    /** 毕业院校 */
    private String graduationSchool;
}
