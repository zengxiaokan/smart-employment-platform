package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职位列表查询请求参数DTO
 */
@Data
public class JobQueryDTO {

    /**
     * 当前页码，默认第1页
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    /**
     * 每页条数，默认5条（和你前端固定5条对应）
     */
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer size = 5;

    /**
     * 搜索关键词（职位名称/公司名称模糊搜索）
     */
    private String keyword;

    /**
     * 城市筛选
     */
    private String city;

    // 职位状态 0-3
    @Min(value = 0, message = "状态不能小于0")
    @Max(value = 3, message = "状态不能大于3")
    private Byte status;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    // 可选：如果后续要加其他筛选条件，比如薪资、经验，也可以在这里扩展
    // private String experience;
    // private Integer salaryMin;
    // private Integer salaryMax;
}