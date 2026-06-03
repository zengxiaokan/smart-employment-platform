package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员查看验证码记录（开发/演示环境用，正式上线后可移除）
 */
@RestController("adminVerificationCodeController")
@RequestMapping("/admin/verification")
public class VerificationCodeController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/codes")
    public Result<List<Map<String, Object>>> getRecentCodes() {
        List<String> raw = redisTemplate.opsForList().range("verification:recent", 0, 49);
        List<Map<String, Object>> list = new ArrayList<>();
        if (raw != null) {
            for (String s : raw) {
                try {
                    list.add(JSON.parseObject(s, Map.class));
                } catch (Exception ignored) {}
            }
        }
        return Result.success(list);
    }
}
