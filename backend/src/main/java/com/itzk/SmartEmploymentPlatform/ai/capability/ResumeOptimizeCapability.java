package com.itzk.SmartEmploymentPlatform.ai.capability;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.ai.model.EduForAI;
import com.itzk.SmartEmploymentPlatform.ai.model.ExpForAI;
import com.itzk.SmartEmploymentPlatform.ai.model.ProjForAI;
import com.itzk.SmartEmploymentPlatform.ai.model.ResumeForAI;
import com.itzk.SmartEmploymentPlatform.ai.prompt.ResumePromptStrategy;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeEducation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeExperience;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeProject;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SaveResumeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ResumeOptimizeCapability {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${minimax.api.url}")
    private String apiUrl;

    @Value("${minimax.api.key}")
    private String apiKey;

    @Value("${minimax.api.model}")
    private String model;

    @Autowired
    private ResumePromptStrategy promptStrategy;

    private final RestTemplate restTemplate = new RestTemplate();

    public SaveResumeDTO optimize(SaveResumeDTO original) {
        // ① 脱敏
        ResumeForAI aiInput = desensitize(original);

        // ② 构建提示词
        String systemPrompt = promptStrategy.buildSystemPrompt();
        String jsonString = JSON.toJSONString(aiInput);
        String userMessage = promptStrategy.buildUserMessage(jsonString);

        // ③ 调用 MiniMax API
        String aiResponse = callMiniMax(systemPrompt, userMessage);
        log.info("AI原始返回: {}", aiResponse);

        // ④ 解析与校验
        ResumeForAI aiOutput = JSON.parseObject(aiResponse, ResumeForAI.class);
        validate(aiOutput, aiInput);

        // ⑤ 回填真实数据
        SaveResumeDTO result = backfill(original, aiOutput);

        // ⑥ 返回
        return result;
    }

    /**
     * ① 脱敏：将敏感字段替换为占位符，日期转字符串
     */
    private ResumeForAI desensitize(SaveResumeDTO dto) {
        ResumeForAI ai = new ResumeForAI();
        ai.setName("[NAME]");
        ai.setPhone("[PHONE]");
        ai.setEmail("[EMAIL]");
        ai.setAge(dto.getAge());
        ai.setGender(dto.getGender());
        ai.setSelfDescription(dto.getSelfDescription());
        ai.setJobIntention(dto.getJobIntention());
        ai.setCity(dto.getCity());
        ai.setSalaryMin(dto.getSalaryMin());
        ai.setSalaryMax(dto.getSalaryMax());
        ai.setSkills(dto.getSkills());
        ai.setResumeName(dto.getResumeName());
        ai.setJobType(dto.getJobType());
        ai.setIndustry(dto.getIndustry());
        ai.setAvailableFrom(dto.getAvailableFrom());

        // 教育经历
        if (dto.getEducations() != null) {
            List<EduForAI> eduList = new ArrayList<>();
            for (ResumeEducation edu : dto.getEducations()) {
                EduForAI e = new EduForAI();
                e.setId(edu.getId());
                e.setSchool("[SCHOOL_" + edu.getId() + "]");
                e.setMajor(edu.getMajor());
                e.setEducation(edu.getEducation());
                e.setDescription(edu.getDescription());
                e.setStartTime(edu.getStartTime() != null ? edu.getStartTime().format(DATE_FMT) : null);
                e.setEndTime(edu.getEndTime() != null ? edu.getEndTime().format(DATE_FMT) : null);
                eduList.add(e);
            }
            ai.setEducations(eduList);
        }

        // 工作经历
        if (dto.getExperiences() != null) {
            List<ExpForAI> expList = new ArrayList<>();
            for (ResumeExperience exp : dto.getExperiences()) {
                ExpForAI e = new ExpForAI();
                e.setId(exp.getId());
                e.setCompany("[COMPANY_" + exp.getId() + "]");
                e.setPosition(exp.getPosition());
                e.setDescription(exp.getDescription());
                e.setStartTime(exp.getStartTime() != null ? exp.getStartTime().format(DATE_FMT) : null);
                e.setEndTime(exp.getEndTime() != null ? exp.getEndTime().format(DATE_FMT) : null);
                expList.add(e);
            }
            ai.setExperiences(expList);
        }

        // 项目经验
        if (dto.getProjects() != null) {
            List<ProjForAI> projList = new ArrayList<>();
            for (ResumeProject proj : dto.getProjects()) {
                ProjForAI p = new ProjForAI();
                p.setId(proj.getId());
                p.setName(proj.getName());
                p.setRole(proj.getRole());
                p.setDescription(proj.getDescription());
                p.setStartTime(proj.getStartTime() != null ? proj.getStartTime().format(DATE_FMT) : null);
                p.setEndTime(proj.getEndTime() != null ? proj.getEndTime().format(DATE_FMT) : null);
                projList.add(p);
            }
            ai.setProjects(projList);
        }

        return ai;
    }

    /**
     * ③ 调用 MiniMax API
     */
    private String callMiniMax(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("max_tokens", 4096);
            body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ));

            log.info("调用MiniMax API, url={}, model={}", apiUrl, body.get("model"));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, request, Map.class);

            log.info("MiniMax API响应状态: {}", resp.getStatusCode());
            if (resp.getBody() == null) {
                log.error("MiniMax API返回body为null, 完整响应: {}", resp);
                throw new BusinessException("MiniMax API返回为空");
            }
            log.info("MiniMax API响应体keys: {}", resp.getBody().keySet());

            if (resp.getBody().get("choices") == null) {
                log.error("MiniMax API返回无choices字段, 完整body: {}", JSON.toJSONString(resp.getBody()));
                throw new BusinessException("MiniMax API返回异常: " + JSON.toJSONString(resp.getBody()));
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (choices.isEmpty()) {
                throw new BusinessException("MiniMax API未返回有效选择");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            // 清理可能的 markdown 代码块包裹
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }

            // 清理推理模型的 <think>...</think> 标签 (MiniMax M2.7等)
            String closeTag = "</think>";
            int thinkEnd = content.lastIndexOf(closeTag);
            if (thinkEnd != -1) {
                content = content.substring(thinkEnd + closeTag.length()).trim();
            }

            content = content.trim();

            log.info("AI解析后内容前100字符: {}", content.length() > 100 ? content.substring(0, 100) : content);

            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用MiniMax API失败", e);
            throw new BusinessException("AI服务调用失败，请稍后重试");
        }
    }

    /**
     * ④ 校验 AI 返回结果
     * 数组长度不一致不再抛异常，而是用原值校正（多了截断、少了补 null），
     * backfill 时取到 null 就用原 description 兜底——保证调用方永远拿到合法 DTO。
     */
    private void validate(ResumeForAI after, ResumeForAI original) {
        // 占位符不能被篡改（这部分必须硬错，关系到隐私）
        if (!"[NAME]".equals(after.getName())) {
            throw new BusinessException("AI篡改了姓名占位符");
        }
        if (!"[PHONE]".equals(after.getPhone())) {
            throw new BusinessException("AI篡改了手机号占位符");
        }
        if (!"[EMAIL]".equals(after.getEmail())) {
            throw new BusinessException("AI篡改了邮箱占位符");
        }

        // 数组长度校正：原数组为空时不允许 AI 补全条目
        after.setEducations(safeResize(after.getEducations(), original.getEducations(), "教育经历"));
        after.setExperiences(safeResize(after.getExperiences(), original.getExperiences(), "工作经历"));
        after.setProjects(safeResize(after.getProjects(), original.getProjects(), "项目经验"));

        // 教育经历中学校占位符检查
        if (original.getEducations() != null) {
            for (int i = 0; i < original.getEducations().size(); i++) {
                String origSchool = original.getEducations().get(i).getSchool();
                EduForAI afterEdu = after.getEducations().get(i);
                if (afterEdu == null) continue;
                String afterSchool = afterEdu.getSchool();
                if (afterSchool != null && !origSchool.equals(afterSchool)) {
                    throw new BusinessException("AI篡改了学校名称占位符: " + origSchool + " -> " + afterSchool);
                }
            }
        }

        // 工作经历中公司占位符检查
        if (original.getExperiences() != null) {
            for (int i = 0; i < original.getExperiences().size(); i++) {
                String origCompany = original.getExperiences().get(i).getCompany();
                ExpForAI afterExp = after.getExperiences().get(i);
                if (afterExp == null) continue;
                String afterCompany = afterExp.getCompany();
                if (afterCompany != null && !origCompany.equals(afterCompany)) {
                    throw new BusinessException("AI篡改了公司名称占位符: " + origCompany + " -> " + afterCompany);
                }
            }
        }
    }

    /**
     * 数组长度校正：多了截断、少了补 null
     * 关键：原数组为 null 或空 → 直接清空 AI 返回（不允许幻觉补全）
     */
    private <T> List<T> safeResize(List<T> optimized, List<T> original, String name) {
        if (original == null || original.isEmpty()) {
            if (optimized != null && !optimized.isEmpty()) {
                log.warn("AI 试图为空的 {} 数组补全条目，已全部丢弃", name);
            }
            return new ArrayList<>();
        }
        if (optimized == null) {
            return new ArrayList<>(Collections.nCopies(original.size(), null));
        }
        if (optimized.size() == original.size()) {
            return optimized;
        }
        if (optimized.size() > original.size()) {
            log.warn("AI 返回的 {} 数量({})超过原数量({})，已截断",
                    name, optimized.size(), original.size());
            return new ArrayList<>(optimized.subList(0, original.size()));
        }
        // 不足：补 null（backfill 时取到 null 用原值兜底）
        List<T> padded = new ArrayList<>(optimized);
        while (padded.size() < original.size()) {
            padded.add(null);
        }
        log.warn("AI 返回的 {} 数量({})少于原数量({})，已用 null 填充",
                name, optimized.size(), original.size());
        return padded;
    }

    /**
     * ⑤ 回填真实数据：将 AI 优化后的文本字段填回原始 DTO
     * 关键：AI 改变数组数量时，optimized[i] 可能为 null，此时用原值兜底（保证不丢条目）
     */
    private SaveResumeDTO backfill(SaveResumeDTO original, ResumeForAI optimized) {
        SaveResumeDTO result = new SaveResumeDTO();
        BeanUtils.copyProperties(original, result);

        // 顶层文本：AI 给了新值就用，否则保留原值（避免 AI 输出 null 时把用户内容清空）
        if (optimized.getSelfDescription() != null) {
            result.setSelfDescription(optimized.getSelfDescription());
        }
        if (optimized.getJobIntention() != null) {
            result.setJobIntention(optimized.getJobIntention());
        }
        if (optimized.getSkills() != null) {
            result.setSkills(optimized.getSkills());
        }

        // 教育经历：保留原始学校名，只取AI优化后的 description
        if (original.getEducations() != null) {
            List<ResumeEducation> eduList = new ArrayList<>();
            List<EduForAI> optEdus = optimized.getEducations();
            for (int i = 0; i < original.getEducations().size(); i++) {
                ResumeEducation origEdu = original.getEducations().get(i);
                ResumeEducation newEdu = new ResumeEducation();
                BeanUtils.copyProperties(origEdu, newEdu);
                if (optEdus != null && i < optEdus.size() && optEdus.get(i) != null) {
                    String optDesc = optEdus.get(i).getDescription();
                    if (optDesc != null) {
                        newEdu.setDescription(optDesc);
                    }
                }
                eduList.add(newEdu);
            }
            result.setEducations(eduList);
        }

        // 工作经历：保留原始公司名，只取AI优化后的 description
        if (original.getExperiences() != null) {
            List<ResumeExperience> expList = new ArrayList<>();
            List<ExpForAI> optExps = optimized.getExperiences();
            for (int i = 0; i < original.getExperiences().size(); i++) {
                ResumeExperience origExp = original.getExperiences().get(i);
                ResumeExperience newExp = new ResumeExperience();
                BeanUtils.copyProperties(origExp, newExp);
                if (optExps != null && i < optExps.size() && optExps.get(i) != null) {
                    String optDesc = optExps.get(i).getDescription();
                    if (optDesc != null) {
                        newExp.setDescription(optDesc);
                    }
                }
                expList.add(newExp);
            }
            result.setExperiences(expList);
        }

        // 项目经验：保留原始数据，只取AI优化后的 description
        if (original.getProjects() != null) {
            List<ResumeProject> projList = new ArrayList<>();
            List<ProjForAI> optProjs = optimized.getProjects();
            for (int i = 0; i < original.getProjects().size(); i++) {
                ResumeProject origProj = original.getProjects().get(i);
                ResumeProject newProj = new ResumeProject();
                BeanUtils.copyProperties(origProj, newProj);
                if (optProjs != null && i < optProjs.size() && optProjs.get(i) != null) {
                    String optDesc = optProjs.get(i).getDescription();
                    if (optDesc != null) {
                        newProj.setDescription(optDesc);
                    }
                }
                projList.add(newProj);
            }
            result.setProjects(projList);
        }

        return result;
    }
}
