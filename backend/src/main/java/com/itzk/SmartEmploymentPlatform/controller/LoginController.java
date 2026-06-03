package com.itzk.SmartEmploymentPlatform.controller;


import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ChangePwdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ForgotPwdCodeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginPhoneDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.RegisterFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResetPwdDTO;
import com.itzk.SmartEmploymentPlatform.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/loginpwd")
    public Result loginPwd(@RequestBody LoginFromDTO loginFromDTO) {
        log.info("登录:{}", loginFromDTO);
        return userService.login(loginFromDTO);
    }

    @PostMapping("/loginphone")
    public Result loginCode(@RequestBody LoginPhoneDTO dto) {
        log.info("短信登录:{}", dto);
        return userService.loginByPhone(dto);
    }

    @PostMapping("/code")
    public Result code(@RequestBody LoginPhoneDTO loginPhoneDTO) {
        String phone = loginPhoneDTO.getPhone();
        log.info("电话号码为:{}", phone);
        return userService.getCode(phone);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterFromDTO register) {
        log.info("注册:{}", register);
        return userService.register(register);
    }


    @PostMapping("changepwd")
    public Result changePwd(@RequestBody ChangePwdDTO changePwdDTO) {
        return userService.changePwd(changePwdDTO);
    }

    @PostMapping("/forgot-pwd/code")
    public Result forgotPwdCode(@RequestBody ForgotPwdCodeDTO dto) {
        log.info("忘记密码-发送验证码: username={}, phone={}", dto.getUsername(), dto.getPhone());
        return userService.sendForgotPwdCode(dto);
    }

    @PostMapping("/forgot-pwd/verify")
    public Result forgotPwdVerify(@RequestBody ForgotPwdCodeDTO dto) {
        log.info("忘记密码-验证验证码: username={}, phone={}", dto.getUsername(), dto.getPhone());
        return userService.verifyForgotPwdCode(dto);
    }

    @PostMapping("/forgot-pwd/reset")
    public Result forgotPwdReset(@RequestBody ResetPwdDTO dto) {
        log.info("忘记密码-重置密码: username={}", dto.getUsername());
        return userService.resetPassword(dto);
    }
}
