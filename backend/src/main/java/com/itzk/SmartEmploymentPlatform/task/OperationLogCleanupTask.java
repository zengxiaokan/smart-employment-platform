package com.itzk.SmartEmploymentPlatform.task;

import com.itzk.SmartEmploymentPlatform.mapper.OperationLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationLogCleanupTask {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanOldLogs() {
        log.info("开始清除 90 天前的操作日志");
        try {
            int deleted = operationLogMapper.deleteOlderThan(90);
            log.info("清除完成，共删除 {} 条日志", deleted);
        } catch (Exception e) {
            log.error("清除操作日志失败", e);
        }
    }
}
