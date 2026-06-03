package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFromDTO {

    //用户名
    @NotBlank
    private String username;

    //电话
    private String phone;

    //密码
    @NotBlank
    private String password;
}