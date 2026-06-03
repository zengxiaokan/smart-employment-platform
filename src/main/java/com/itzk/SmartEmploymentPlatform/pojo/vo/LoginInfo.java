package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginInfo {

    //用户id
    private Long userId;
    //用户身份
    private Byte role;

    //用户名
    private String username;

    //用户昵称
    private String nickname;

    //用户单位
    private Long companyId;

    //用户头像
    private String avatarUrl;

    //单位名称
    private String companyName;

    //登录令牌
    private String token;


}
