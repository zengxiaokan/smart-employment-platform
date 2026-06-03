package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 * 用于返回用户详情，包含用户基本信息（不包含敏感信息如密码）
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 用户角色：0-求职者，1-HR，2-管理员 */
    private Byte role;

    /** 账号状态：0-禁用，1-正常 */
    private Byte status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
