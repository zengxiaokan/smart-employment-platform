package com.itzk.SmartEmploymentPlatform.controller.recruitment;


import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfoVo;
import com.itzk.SmartEmploymentPlatform.service.UserService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class HrController {


    @Autowired
    private UserService userService;


    @GetMapping("/hr/profile")
    public Result<UserInfoVo> getUserInfo(){
        Long userId = UserHolder.getUserId();
        log.info("查询个人信息"+userId);
        return userService.getUserInfo(userId);
    }

    @PutMapping("/hr/profile")
    public Result updateUserInfo(@RequestBody UserInfoVo userInfoVo){


        Long userId = UserHolder.getUserId();
        log.info("修改个人信息"+userId);
        userInfoVo.setUserId(userId);



        return userService.updateByInfo(userInfoVo);
    }


}
