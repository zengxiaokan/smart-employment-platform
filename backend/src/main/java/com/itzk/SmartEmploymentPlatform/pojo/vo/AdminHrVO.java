package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

/**
 * 管理员 HR 列表项 — role=1，含审核状态和公司名
 */
@Data
public class AdminHrVO {
    private Long id;
    private String username;
    private String phone;
    private String avatarUrl;
    private String realname;
    private String nickname;
    private String city;
    private String companyName;
    private Integer status;
    private Integer auditStatus;
    private Integer gender;
    private Integer age;
    private String createdAt;
}
