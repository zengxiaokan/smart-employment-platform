package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 职位申请列表查询请求参数DTO
 * 用于分页查询职位申请记录，支持职位、申请人、公司、状态等条件筛选
 */
@Data
public class ApplicationQueryDTO {

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

    /** 职位ID */
    private Long jobTitle;

    /** 申请人用户ID */
    private Long applicantName;

    /** 公司ID */
    private Long companyId;

    /** 申请状态：0-待处理，1-已查看，2-邀请面试，3-不合适 */
    private Byte status;
}
