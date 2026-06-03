package com.itzk.SmartEmploymentPlatform.pojo.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ApplicationDitailVo {

    //投递人
    private String realname;

    //投递时间
    private LocalDateTime appliedAt;

    //面试时间
    private LocalDateTime interviewTime;

    //投递状态
    private byte applicationStatus;

    //岗位名称
    private String title;

    // 职位类别
    @Size(max = 50, message = "职位类别长度不能超过50")
    private String category;

    // 薪资下限（最小0）
    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    // 薪资上限
    @Min(value = 0, message = "薪资上限不能小于0")
    private Integer salaryMax;

    // 经验要求
    @Size(max = 30, message = "经验要求长度不能超过30")
    private String experience;

    // 学历要求 0-7（0=不限 1=初中 2=高中 3=中专 4=大专 5=本科 6=硕士 7=博士）
    @Min(value = 0, message = "学历值不能小于0")
    @Max(value = 7, message = "学历值不能大于7")
    private Byte education;

    // 标签
    @Size(max = 255, message = "标签长度不能超过255")
    private String tags;

    // 招聘人数（最少1人）
    @Min(value = 1, message = "招聘人数至少1人")
    private Integer headcount;

    // 岗位职责
    private String dutyContent;

    // 任职要求
    private String requireContent;

    // 福利
    private String benefits;

    // 职位状态 0-3
    @NotNull(message = "职位状态不能为空")
    @Min(value = 0, message = "状态不能小于0")
    @Max(value = 3, message = "状态不能大于3")
    private Byte jobStatus;

    // 浏览量
    @Min(value = 0, message = "浏览量不能为负数")
    private Integer viewCount;

    // 投递量
    @Min(value = 0, message = "投递量不能为负数")
    private Integer applyCount;


    /** 企业名称 */
    private String name;

    /** 企业简介 */
    private String description;

    /** 所属行业 */
    private String industry;

    /** 企业规模：0-0-20人，1-20-99人，2-100-499人，3-500-999人，4-1000人以上 */
    private String size;

    /** 所在城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 企业Logo URL */
    private String logoUrl;

    //操作人id
    private Long operatorId;

    //hrid方便联系
    private Long hrId;



}
