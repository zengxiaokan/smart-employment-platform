package com.itzk.SmartEmploymentPlatform.ai.prompt;

/**
 * 匹配推荐语Prompt策略
 * 仿照ResumePromptStrategy，生成AI推荐语的System/User提示词
 */
public interface MatchSummaryPromptStrategy {

    /** 系统角色与输出规范 */
    String buildSystemPrompt();

    /** 匹配数据JSON嵌入User消息 */
    String buildUserMessage(String matchJson);
}
