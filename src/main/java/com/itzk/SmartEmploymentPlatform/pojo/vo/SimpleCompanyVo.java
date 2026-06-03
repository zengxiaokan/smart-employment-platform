package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class SimpleCompanyVo {
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

    /** 发布职位数量 */
    private Integer jobCount;
}
