package com.itzk.SmartEmploymentPlatform.task;

import com.itzk.SmartEmploymentPlatform.mapper.MessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日凌晨清除 15 天前的聊天记录
 */
@Slf4j
@Component
public class ChatCleanupTask {

    @Resource
    private MessageMapper messageMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldMessages() {
        log.info("开始清除 15 天前的聊天记录");
        try {
            int deleted = messageMapper.deleteOlderThan(15);
            log.info("清除完成，共删除 {} 条消息", deleted);
        } catch (Exception e) {
            log.error("清除聊天记录失败", e);
        }
    }
}
