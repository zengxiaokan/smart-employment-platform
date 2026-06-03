package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPwdCodeDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 验证码（仅验证步骤使用，发送时可不传） */
    private String code;
}
