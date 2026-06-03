package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;

@Data
public class ChangePwdDTO {

    private String oldPassword;
    private String newPassword;

}
