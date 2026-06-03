package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;


import lombok.Data;

@Data
public class CompanyFromDTO {

    /** 企业名称 */
    private String companyName;
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
    //企业官网
    private String officialWeb;
    private String phone;                // 企业联系电话
    //融资阶段
    private  byte financingStage;

    private Long userId;                 // 申请人用户ID

    private Byte auditStatus;              // 申请状态：0=待审核 1=审核通过 2=审核拒绝 3=已撤回




}
