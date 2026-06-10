package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.mapper.MessageMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private MessageMapper messageMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> latest(@Min(1) @RequestParam(defaultValue = "5") int limit) {
        List<Message> messages = messageMapper.selectLatestAnnouncements(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message m : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("sentAt", m.getSentAt());
            try {
                Map<String, Object> parsed = JSON.parseObject(m.getContent(), Map.class);
                item.put("title", parsed.getOrDefault("title", ""));
                item.put("content", parsed.getOrDefault("content", ""));
                item.put("pinned", parsed.getOrDefault("pinned", false));
            } catch (Exception e) {
                log.warn("解析公告内容JSON失败", e);
                item.put("title", "");
                item.put("content", m.getContent());
                item.put("pinned", false);
            }
            result.add(item);
        }
        return Result.success(result);
    }
}
