package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 企业视图对象
 * 用于返回企业详情，包含企业基本信息及发布的职位数量
 */
@Data
public class CompanyVO {

    /** 企业ID */
    private Long id;

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

    /** 营业执照URL */
    private String licenseUrl;

    /** 审核状态：0-待审核，1-已通过，2-未通过 */
    private Byte auditStatus;

    /** 发布职位数量 */
    private Integer jobCount;

    //融资阶段
    private  byte financingStage;

    //企业官网
    private String officialWeb;

    /** 创建时间 */
    private LocalDateTime createdAt;

    //联系电话
    private String phone;
}
