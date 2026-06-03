package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员最新注册用户
 */
@Data
public class AdminRecentUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private Integer role;
    private LocalDateTime createdAt;
}
