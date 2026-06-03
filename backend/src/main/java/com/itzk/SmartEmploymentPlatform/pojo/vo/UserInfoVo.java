package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class UserInfoVo {
    //用户id
    private Long userId;

    //用户名
    private String realname;

    //用户昵称
    private String nickname;

    private String phone;

    private Byte gender;

    private Integer age;

    private String email;

    private String city;

    //用户身份
    private Byte role;

    private String avatarUrl;
}
