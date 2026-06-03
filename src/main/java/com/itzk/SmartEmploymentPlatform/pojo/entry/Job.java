package com.itzk.SmartEmploymentPlatform.pojo.entry;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Job {

    // 职位ID（新增时不需要，修改时不能为空）
    @Null(message = "新增职位时ID必须为空", groups = Create.class)
    @NotNull(message = "职位ID不能为空", groups = Update.class)
    private Long id;

    // 企业ID（必须传）
    @NotNull(message = "企业ID不能为空")
    private Long companyId;

    // HR用户ID（必须传）
    @NotNull(message = "发布人ID不能为空")
    private Long hrUserId;

    // 职位名称（必须传，长度1-100）
    @NotBlank(message = "职位名称不能为空")
    @Size(min = 2, max = 100, message = "职位名称长度在2-100个字符")
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

    // 城市（必须传）
    @NotBlank(message = "工作城市不能为空")
    private String city;

    // 详细地址
    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

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

    //技能
    private String jobSkills;

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
    private Byte status;

    // 浏览量
    @Min(value = 0, message = "浏览量不能为负数")
    private Integer viewCount;

    // 投递量
    @Min(value = 0, message = "投递量不能为负数")
    private Integer applyCount;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    //剩余岗位
    private int hasCount;

    //待审核岗位调整数
    private int pendingAdjust;

    // 分组校验用（新增/修改区分）
    public interface Create {}
    public interface Update {}
}