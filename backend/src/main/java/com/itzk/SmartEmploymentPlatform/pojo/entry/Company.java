package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 企业实体类
 * 对应数据库中的企业表，存储企业的基本信息
 */
@Data
public class Company {

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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    //岗位数量
    private Integer jobCount;


    //已入职岗位数
    private Integer jobConfirm;

    //融资阶段
    private  Byte financingStage;

    //企业官网
    private String officialWeb;

    //联系电话
    private String phone;

    //
    private Long userId;
}
