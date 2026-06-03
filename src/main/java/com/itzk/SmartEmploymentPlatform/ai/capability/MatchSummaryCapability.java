package com.itzk.SmartEmploymentPlatform.ai.capability;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.ai.prompt.MatchSummaryPromptStrategy;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匹配推荐语AI能力
 * 复用MiniMax API，生成50字以内个性化推荐语
 * 仿照ResumeOptimizeCapability，但无需脱敏/回填——只生成纯文本
 */
@Slf4j
@Component
public class MatchSummaryCapability {

    @Value("${minimax.api.url}")
    private String apiUrl;

    @Value("${minimax.api.key}")
    private String apiKey;

    @Value("${minimax.api.model}")
    private String model;

    @Autowired
    private MatchSummaryPromptStrategy promptStrategy;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(Map<String, Object> matchData) {
        String systemPrompt = promptStrategy.buildSystemPrompt();
        String jsonString = JSON.toJSONString(matchData);
        String userMessage = promptStrategy.buildUserMessage(jsonString);

        String result = callMiniMax(systemPrompt, userMessage);
        log.info("AI推荐语: {}", result);
        return result;
    }

    private String callMiniMax(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1024);
            body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (resp.getBody() == null) {
                throw new BusinessException("MiniMax API返回为空");
            }

            if (resp.getBody().get("choices") == null) {
                log.error("MiniMax API返回异常: {}", JSON.toJSONString(resp.getBody()));
                throw new BusinessException("MiniMax API返回异常");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (choices.isEmpty()) {
                throw new BusinessException("MiniMax API未返回有效选择");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = ((String) message.get("content")).trim();

            // 清理 <think>...</think> 标签（推理模型可能把内容全包在think里）
            String closeTag = "</think>";
            int thinkClose = content.lastIndexOf(closeTag);
            if (thinkClose != -1) {
                content = content.substring(thinkClose + closeTag.length()).trim();
            }
            // 如果还有开标签没闭合，去掉开标签之前的内容
            String openTag = "<think>";
            int thinkOpen = content.indexOf(openTag);
            if (thinkOpen != -1) {
                content = content.substring(thinkOpen + openTag.length()).trim();
            }

            // 截断到200字
            if (content.length() > 200) {
                content = content.substring(0, 200);
            }

            if (content.isEmpty()) {
                throw new BusinessException("AI返回空内容");
            }

            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用MiniMax API失败", e);
            throw new BusinessException("AI服务调用失败，请稍后重试");
        }
    }
}
