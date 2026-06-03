package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginPhoneDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone; // 和数据库字段完全一致

    @NotBlank(message = "验证码不能为空")
    private String code;

    private Byte role;
}