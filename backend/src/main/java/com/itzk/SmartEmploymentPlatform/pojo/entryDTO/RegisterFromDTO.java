package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFromDTO {

    //用户名
    @NotBlank
    private String username;

    //电话
    @NotBlank
    private String phone;

    //密码
    @NotBlank
    private String password;


    //角色 0用户 1hr
    @NotNull
    private Byte role;


}
