package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的用户表，存储用户的基本信息
 */
@Data
public class User {

    /** 用户ID */
    private Long id;

    private String username;

    /** 密码哈希值 */
    private String password;

    private String realname;

    private Byte gender;

    private Integer age;


    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;


    private String city;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 用户角色：0-求职者，1-HR，2-公司管理 3-管理员 */
    private Byte role;

    /** 账号状态：0-禁用，1-正常 */
    private Byte status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    //属于哪一家单位
    private Long companyId;

    //单位名称
    private String companyName;
}
