package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 企业列表查询请求参数DTO
 * 用于分页查询企业列表，支持关键词、行业、城市、审核状态等条件筛选
 */
@Data
public class CompanyQueryDTO {

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

    //行业规模
    private String companySize;

    /** 搜索关键词（企业名称模糊搜索） */
    private String keyword;

    /** 所属行业 */
    private String industry;

    /** 所在城市 */
    private String city;

    /** 审核状态：0-待审核，1-已通过，2-未通过 */
    private Byte auditStatus;
}
