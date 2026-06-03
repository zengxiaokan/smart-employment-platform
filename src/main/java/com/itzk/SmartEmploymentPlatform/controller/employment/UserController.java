package com.itzk.SmartEmploymentPlatform.controller.employment;


import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfoVo;
import com.itzk.SmartEmploymentPlatform.service.UserService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/profile")
    public Result<UserInfoVo> getUserInfo(){
        Long userId = UserHolder.getUserId();
        log.info("查询个人信息"+userId);
        return userService.getUserInfo(userId);
    }

    @PutMapping("/user/profile")
    public Result updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        Long userId = UserHolder.getUserId();
        log.info("修改个人信息"+userId);
        userInfoVo.setUserId(userId);
        return userService.updateByInfo(userInfoVo);
    }

    /** 个人中心统计：收藏、投递、面试数量（Redis缓存5分钟） */
    @GetMapping("/user/stats")
    public Result<Map<String, Integer>> getUserStats(){
        Long userId = UserHolder.getUserId();
        return userService.getUserStats(userId);
    }

    /** 首页统计：在招岗位、入驻企业、求职者、成功匹配（Redis缓存30分钟） */
    @GetMapping("/user/home/stats")
    public Result<Map<String, Object>> getHomeStats(){
        return userService.getHomeStats();
    }

}
