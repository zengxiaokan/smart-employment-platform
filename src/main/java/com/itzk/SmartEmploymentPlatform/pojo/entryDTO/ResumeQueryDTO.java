package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 简历列表查询请求参数DTO
 * 用于分页查询简历列表，支持关键词、城市、求职意向等条件筛选
 */
@Data
public class ResumeQueryDTO {

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
}
