package com.itzk.SmartEmploymentPlatform.task;

import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日23:55执行，将当天及之前已过面试时间但仍未处理的面试标记为"已过期"，
 * 同时将关联的申请表状态改为"已取消"
 */
@Slf4j
@Component
public class InterviewExpireTask {

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private ApplicationMapper applicationMapper;

    @Scheduled(cron = "0 55 23 * * ?")
    public void expireInterviews() {
        List<Interview> expired = interviewMapper.selectExpired();
        if (expired.isEmpty()) {
            return;
        }
        log.info("发现 {} 条已过期面试，开始处理", expired.size());
        for (Interview iv : expired) {
            try {
                interviewMapper.expireById(iv.getId());
                applicationMapper.setStatusById(iv.getApplicationId(), Constant.ApplicationStatus.APPLY_CANCER);
                log.info("面试 {} 已过期，申请表 {} 已取消", iv.getId(), iv.getApplicationId());
            } catch (Exception e) {
                log.error("处理过期面试失败, interviewId={}", iv.getId(), e);
            }
        }
        log.info("过期面试处理完成，共处理 {} 条", expired.size());
    }
}
