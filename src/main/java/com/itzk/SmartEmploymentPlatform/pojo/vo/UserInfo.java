package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    //用户id
    private Long userId;

    private String username;

    //用户身份
    private Byte role;

    //
    private Long companyId;

    //是否封号
    private Byte status;
}
