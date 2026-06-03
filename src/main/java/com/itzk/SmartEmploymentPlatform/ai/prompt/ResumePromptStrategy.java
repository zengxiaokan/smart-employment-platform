package com.itzk.SmartEmploymentPlatform.ai.prompt;

/**
 * 简历优化提示词策略接口
 */
public interface ResumePromptStrategy {

    /** 构建系统指令 */
    String buildSystemPrompt();

    /** 构建用户消息 */
    String buildUserMessage(String resumeJson);
}
