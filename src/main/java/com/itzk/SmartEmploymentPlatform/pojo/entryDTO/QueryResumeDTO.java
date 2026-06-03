package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class QueryResumeDTO {


    /**
     * 当前页码，默认第1页
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    /**
     * 每页条数，默认10条
     */
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer size = 10;

    /** 搜索关键词（姓名、求职意向等模糊搜索） */
    private String keyword;

    /** 期望工作城市 */
    private String city;

    /** 求职意向 */
    private String jobIntention;

    /** 用户ID */
    private Long userId;

    /** 总工作年限（冗余字段，避免联表查履历） */
    private Integer totalWorkYears;

    /** 最高学历（冗余字段，0-6对应Education枚举） */
    private Byte maxEducation;

    /** 期望最低薪资 */
    private Integer salaryMin;

    /** 期望最高薪资 */
    private Integer salaryMax;

}
