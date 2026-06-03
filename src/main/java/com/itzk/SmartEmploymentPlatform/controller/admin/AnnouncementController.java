package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.mapper.MessageMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController("adminAnnouncementController")
@RequestMapping("/admin/announcements")
public class AnnouncementController {

    @Autowired
    private MessageMapper messageMapper;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        PageHelper.startPage(page, size);
        List<Message> list = messageMapper.selectAnnouncements(UserHolder.getUserId());
        List<Map<String, Object>> items = new ArrayList<>();
        for (Message m : list) {
            items.add(toAnnVo(m));
        }
        PageInfo<Message> pageInfo = new PageInfo<>(list);
        return Result.success(new PageResult(pageInfo.getTotal(), items));
    }

    @PostMapping
    public Result create(@RequestBody Map<String, String> body) {
        Message msg = new Message();
        msg.setConversationId(0L);
        msg.setSenderId(UserHolder.getUserId());
        msg.setReceiverId(0L);
        msg.setContent(toJson(body.get("title"), body.get("content")));
        msg.setMsgType(1);
        msg.setIsRead(0);
        messageMapper.insert(msg);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        messageMapper.updateContent(id, toJson(body.get("title"), body.get("content")));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        messageMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/pin")
    public Result togglePin(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean pinned = body.getOrDefault("pinned", false);
        Message m = messageMapper.selectById(id);
        if (m == null) return Result.error("公告不存在");
        try {
            Map<String, Object> parsed = JSON.parseObject(m.getContent(), Map.class);
            parsed.put("pinned", pinned);
            messageMapper.updateContent(id, JSON.toJSONString(parsed));
        } catch (Exception e) {
            Map<String, Object> updated = new LinkedHashMap<>();
            updated.put("title", "");
            updated.put("content", "");
            updated.put("pinned", pinned);
            messageMapper.updateContent(id, JSON.toJSONString(updated));
        }
        return Result.success();
    }

    private String toJson(String title, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title != null ? title : "");
        m.put("content", content != null ? content : "");
        m.put("pinned", false);
        return JSON.toJSONString(m);
    }

    private Map<String, Object> toAnnVo(Message m) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", m.getId());
        vo.put("createdAt", m.getSentAt());
        vo.put("updatedAt", m.getSentAt());
        try {
            Map<String, Object> parsed = JSON.parseObject(m.getContent(), Map.class);
            vo.put("title", parsed.getOrDefault("title", ""));
            vo.put("content", parsed.getOrDefault("content", ""));
            vo.put("pinned", parsed.getOrDefault("pinned", false));
        } catch (Exception e) {
            vo.put("title", "");
            vo.put("content", m.getContent());
            vo.put("pinned", false);
        }
        return vo;
    }
}
