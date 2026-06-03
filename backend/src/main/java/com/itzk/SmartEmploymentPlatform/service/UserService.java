package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ChangePwdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ForgotPwdCodeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginPhoneDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.RegisterFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResetPwdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.LoginInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfoVo;


import java.util.Map;

public interface UserService {
    //获取验证码
    Result getCode(String  phone);

    //用户登录
    Result<LoginInfo> login(LoginFromDTO loginFromDTO);

    //短信验证码登录
    Result<LoginInfo> loginByPhone(LoginPhoneDTO dto);

    Result<LoginInfo> register(RegisterFromDTO register);

    Result<UserInfoVo> getUserInfo(Long  userId);

    Result updateByInfo( UserInfoVo userInfoVo);

    Result changePwd(ChangePwdDTO changePwdDTO);

    /** 忘记密码 — 发送验证码（需校验用户名与手机号匹配） */
    Result sendForgotPwdCode(ForgotPwdCodeDTO dto);

    /** 忘记密码 — 验证验证码是否正确 */
    Result verifyForgotPwdCode(ForgotPwdCodeDTO dto);

    /** 忘记密码 — 验证码通过后重置密码 */
    Result resetPassword(ResetPwdDTO dto);

    /** 个人中心统计：收藏数、投递数、面试数 */
    Result<Map<String, Integer>> getUserStats(Long userId);

    /** 首页统计：在招岗位、入驻企业、求职者、成功匹配 */
    Result<Map<String, Object>> getHomeStats();
}
