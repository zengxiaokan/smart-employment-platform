package com.itzk.SmartEmploymentPlatform.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * 默认匹配推荐语Prompt实现
 * 要求AI每次生成不同的推荐语（50字以内，根据分数侧重点调整语气）
 */
@Component
public class DefaultMatchSummaryPromptStrategy implements MatchSummaryPromptStrategy {

    @Override
    public String buildSystemPrompt() {
        return "你是一位专业的企业招聘分析顾问。根据候选人的技能匹配、经验、学历和薪资匹配数据，" +
               "生成一段50字以上250字以内的个性化推荐语。要求：\n" +
               "1. 直接输出推荐语，禁止输出分析过程、思考过程或解释\n" +
               "2. 每次表述方式必须不同，禁止使用固定模板\n" +
               "3. 根据分数侧重点调整：技能分高则强调技术能力、经验分高则强调行业资历、都均衡则强调综合素质\n" +
               "4. 语气专业、简洁，直接给出结论";
    }

    @Override
    public String buildUserMessage(String matchJson) {
        return "以下是候选人匹配数据，请生成一段200字以内的推荐语：\n" + matchJson;
    }
}
