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
     */
    private void validate(ResumeForAI after, ResumeForAI original) {
        // 占位符不能被篡改
        if (!"[NAME]".equals(after.getName())) {
            throw new BusinessException("AI篡改了姓名占位符");
        }
        if (!"[PHONE]".equals(after.getPhone())) {
            throw new BusinessException("AI篡改了手机号占位符");
        }
        if (!"[EMAIL]".equals(after.getEmail())) {
            throw new BusinessException("AI篡改了邮箱占位符");
        }

        // 数组长度必须一致
        int origEduSize = original.getEducations() != null ? original.getEducations().size() : 0;
        int afterEduSize = after.getEducations() != null ? after.getEducations().size() : 0;
        if (origEduSize != afterEduSize) {
            throw new BusinessException("AI改变了教育经历数量");
        }

        int origExpSize = original.getExperiences() != null ? original.getExperiences().size() : 0;
        int afterExpSize = after.getExperiences() != null ? after.getExperiences().size() : 0;
        if (origExpSize != afterExpSize) {
            throw new BusinessException("AI改变了工作经历数量");
        }

        int origProjSize = original.getProjects() != null ? original.getProjects().size() : 0;
        int afterProjSize = after.getProjects() != null ? after.getProjects().size() : 0;
        if (origProjSize != afterProjSize) {
            throw new BusinessException("AI改变了项目经验数量");
        }

        // 教育经历中学校占位符检查
        if (original.getEducations() != null) {
            for (int i = 0; i < original.getEducations().size(); i++) {
                String origSchool = original.getEducations().get(i).getSchool();
                String afterSchool = after.getEducations().get(i).getSchool();
                if (!origSchool.equals(afterSchool)) {
                    throw new BusinessException("AI篡改了学校名称占位符: " + origSchool + " -> " + afterSchool);
                }
            }
        }

        // 工作经历中公司占位符检查
        if (original.getExperiences() != null) {
            for (int i = 0; i < original.getExperiences().size(); i++) {
                String origCompany = original.getExperiences().get(i).getCompany();
                String afterCompany = after.getExperiences().get(i).getCompany();
                if (!origCompany.equals(afterCompany)) {
                    throw new BusinessException("AI篡改了公司名称占位符: " + origCompany + " -> " + afterCompany);
                }
            }
        }
    }

    /**
     * ⑤ 回填真实数据：将 AI 优化后的文本字段填回原始 DTO
     */
    private SaveResumeDTO backfill(SaveResumeDTO original, ResumeForAI optimized) {
        SaveResumeDTO result = new SaveResumeDTO();
        BeanUtils.copyProperties(original, result);

        // 用AI优化后的文本字段替换
        result.setSelfDescription(optimized.getSelfDescription());
        result.setJobIntention(optimized.getJobIntention());
        result.setSkills(optimized.getSkills());

        // 教育经历：保留原始学校名，只取AI优化后的 description
        if (original.getEducations() != null && optimized.getEducations() != null) {
            List<ResumeEducation> eduList = new ArrayList<>();
            for (int i = 0; i < original.getEducations().size(); i++) {
                ResumeEducation origEdu = original.getEducations().get(i);
                EduForAI optEdu = optimized.getEducations().get(i);

                ResumeEducation newEdu = new ResumeEducation();
                BeanUtils.copyProperties(origEdu, newEdu);
                newEdu.setDescription(optEdu.getDescription());
                eduList.add(newEdu);
            }
            result.setEducations(eduList);
        }

        // 工作经历：保留原始公司名，只取AI优化后的 description
        if (original.getExperiences() != null && optimized.getExperiences() != null) {
            List<ResumeExperience> expList = new ArrayList<>();
            for (int i = 0; i < original.getExperiences().size(); i++) {
                ResumeExperience origExp = original.getExperiences().get(i);
                ExpForAI optExp = optimized.getExperiences().get(i);

                ResumeExperience newExp = new ResumeExperience();
                BeanUtils.copyProperties(origExp, newExp);
                newExp.setDescription(optExp.getDescription());
                expList.add(newExp);
            }
            result.setExperiences(expList);
        }

        // 项目经验：保留原始数据，只取AI优化后的 description
        if (original.getProjects() != null && optimized.getProjects() != null) {
            List<ResumeProject> projList = new ArrayList<>();
            for (int i = 0; i < original.getProjects().size(); i++) {
                ResumeProject origProj = original.getProjects().get(i);
                ProjForAI optProj = optimized.getProjects().get(i);

                ResumeProject newProj = new ResumeProject();
                BeanUtils.copyProperties(origProj, newProj);
                newProj.setDescription(optProj.getDescription());
                projList.add(newProj);
            }
            result.setProjects(projList);
        }

        return result;
    }
}
