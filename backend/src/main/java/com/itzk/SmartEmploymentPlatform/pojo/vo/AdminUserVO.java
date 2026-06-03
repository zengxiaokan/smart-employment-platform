package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

/**
 * 管理员用户列表项 — 求职者 + 管理员（role 0/2/3）
 */
@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String phone;
    private String avatarUrl;
    private String realname;
    private String nickname;
    private String city;
    private Integer role;
    private Integer status;
    private Integer gender;
    private Integer age;
    private String createdAt;
}
