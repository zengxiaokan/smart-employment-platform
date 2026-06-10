package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.*;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResumeQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SaveResumeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.service.ResumeService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/user/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ResumeMapper resumeMapper;



    @GetMapping("/list")
    public Result<PageResult> listResumes(ResumeQueryDTO queryDTO) {
        log.info("查询简历列表: {}", queryDTO);
        Long userId = UserHolder.getUserId();
        queryDTO.setUserId(userId);
        PageResult pageResult = resumeService.listResumes(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<ResumeVO> getResumeDetail(@PathVariable Long id ) {
        log.info("查询简历详情: id={}", id);
        Resume resume = resumeMapper.getResumeById(id);
        if (resume == null || !resume.getUserId().equals(UserHolder.getUserId())) {
            return Result.error("简历不存在");
        }
        ResumeVO vo = resumeService.getResumeDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/save")
    public Result<ResumeVO> saveResume(@Valid @RequestBody SaveResumeDTO saveDTO) {
        Long userId = UserHolder.getUserId();
        saveDTO.setUserId(userId);
        log.info("保存简历: {}", saveDTO);
        ResumeVO vo = resumeService.saveResume(saveDTO);
        return Result.success(vo);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteResume(@PathVariable Long id) {
        log.info("删除简历: id={}", id);
        Resume resume = resumeMapper.getResumeById(id);
        if (resume == null || !resume.getUserId().equals(UserHolder.getUserId())) {
            return Result.error("无权操作该简历");
        }
        resumeService.deleteResume(id);
        return Result.success();
    }

    @PutMapping("/setDefault/{id}")
    public Result<Void> setDefaultResume(@PathVariable Long id) {
        log.info("设置默认简历: id={}", id);
        Resume resume = resumeMapper.getResumeById(id);
        if (resume == null || !resume.getUserId().equals(UserHolder.getUserId())) {
            return Result.error("无权操作该简历");
        }
        return resumeService.setDefaultResume(id);
    }

    @PostMapping("/optimize")
    public Result<SaveResumeDTO> optimize(@Valid @RequestBody SaveResumeDTO dto) {
        log.info("AI优化简历: name={}", dto.getName());
        SaveResumeDTO optimized = resumeService.optimizeResume(dto);
        return Result.success(optimized);
    }

    /** AI 优化页专用：从数据库加载默认简历 → AI 优化 → 返回对比数据 */
    @PostMapping("/ai-optimize")
    public Result<Map<String, Object>> aiOptimize() {
        Long userId = UserHolder.getUserId();
        Resume resume = resumeMapper.getDefaultByuserId(userId);
        if (resume == null) {
            return Result.error("请先创建默认简历");
        }
        SaveResumeDTO dto = toSaveResumeDTO(resume);
        SaveResumeDTO optimized = resumeService.optimizeResume(dto);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("original", dto);
        result.put("optimized", optimized);
        return Result.success(result);
    }

    @PutMapping("/update")
    public Result<ResumeVO> updateResume(@Valid @RequestBody SaveResumeDTO dto) {
        Long userId = UserHolder.getUserId();
        dto.setUserId(userId);
        log.info("更新简历(AI采纳): id={}, name={}", dto.getId(), dto.getName());
        ResumeVO vo = resumeService.saveResume(dto);
        return Result.success(vo);
    }

    private SaveResumeDTO toSaveResumeDTO(Resume r) {
        SaveResumeDTO dto = new SaveResumeDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setResumeName(r.getResumeName());
        dto.setName(r.getName());
        dto.setPhone(r.getPhone());
        dto.setEmail(r.getEmail());
        dto.setAge(r.getAge());
        dto.setGender(r.getGender());
        dto.setSkills(r.getSkills());
        dto.setSelfDescription(r.getSelfDescription());
        dto.setJobIntention(r.getJobIntention());
        dto.setCity(r.getCity());
        dto.setSalaryMin(r.getSalaryMin());
        dto.setSalaryMax(r.getSalaryMax());
        dto.setCharacterAvatar(r.getCharacterAvatar());
        dto.setJobType(r.getJobType());
        dto.setIndustry(r.getIndustry());
        dto.setAvailableFrom(r.getAvailableFrom());
        dto.setGraduationSchool(r.getGraduationSchool());
        Long resumeId = r.getId();
        if (resumeId != null) {
            dto.setEducations(resumeMapper.getEducationsByResumeId(resumeId));
            dto.setExperiences(resumeMapper.getExperiencesByResumeId(resumeId));
            dto.setProjects(resumeMapper.getProjectsByResumeId(resumeId));
        }
        return dto;
    }

    // ==================== 单表操作 ====================

    /**
     * 单独保存一条教育经历（新增或更新）
     */
    @PostMapping("/education")
    public Result<ResumeEducation> saveEducation(@Valid @RequestBody ResumeEducation education) {
        log.info("单独保存教育经历: resumeId={}, id={}", education.getResumeId(), education.getId());
        if (education.getResumeId() != null) {
            Resume owner = resumeMapper.getResumeById(education.getResumeId());
            if (owner == null || !owner.getUserId().equals(UserHolder.getUserId())) {
                return Result.error("无权操作该简历");
            }
        }
        ResumeEducation result = resumeService.saveEducation(education);
        return Result.success(result);
    }

    /**
     * 根据ID删除单条教育经历
     */
    @DeleteMapping("/education/{id}")
    public Result<Void> deleteEducation(@PathVariable Long id) {
        log.info("删除教育经历: id={}", id);
        resumeService.deleteEducation(id);
        return Result.success();
    }

    /**
     * 单独保存一条工作经历（新增或更新）
     */
    @PostMapping("/experience")
    public Result<ResumeExperience> saveExperience(@Valid @RequestBody ResumeExperience experience) {
        log.info("单独保存工作经历: resumeId={}, id={}", experience.getResumeId(), experience.getId());
        if (experience.getResumeId() != null) {
            Resume owner = resumeMapper.getResumeById(experience.getResumeId());
            if (owner == null || !owner.getUserId().equals(UserHolder.getUserId())) {
                return Result.error("无权操作该简历");
            }
        }
        ResumeExperience result = resumeService.saveExperience(experience);
        return Result.success(result);
    }

    /**
     * 根据ID删除单条工作经历
     */
    @DeleteMapping("/experience/{id}")
    public Result<Void> deleteExperience(@PathVariable Long id) {
        log.info("删除工作经历: id={}", id);
        resumeService.deleteExperience(id);
        return Result.success();
    }

    /**
     * 单独保存一条项目经历（新增或更新）
     */
    @PostMapping("/project")
    public Result<ResumeProject> saveProject(@Valid @RequestBody ResumeProject project) {
        log.info("单独保存项目经历: resumeId={}, id={}", project.getResumeId(), project.getId());
        if (project.getResumeId() != null) {
            Resume owner = resumeMapper.getResumeById(project.getResumeId());
            if (owner == null || !owner.getUserId().equals(UserHolder.getUserId())) {
                return Result.error("无权操作该简历");
            }
        }
        ResumeProject result = resumeService.saveProject(project);
        return Result.success(result);
    }

    /**
     * 根据ID删除单条项目经历
     */
    @DeleteMapping("/project/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        log.info("删除项目经历: id={}", id);
        resumeService.deleteProject(id);
        return Result.success();
    }

    /**
     * 单独保存求职意向（更新简历主表的求职意向相关字段）
     * 前端jobType传中文标签（全职/兼职/实习/不限），后端自动转换为Integer
     */
    @PostMapping("/intention")
    public Result<Void> saveJobIntention(@Valid @RequestBody Resume resume) {
        Long userId = UserHolder.getUserId();
        resume.setUserId(userId);
        if (resume.getId() == null) {
            resume.setResumeName("默认简历");
            resume.setIsDefault((byte) 1);
        }
        log.info("单独保存求职意向: id={}, jobIntention={}", resume.getId(), resume.getJobIntention());
        return resumeService.saveJobIntention(resume);
    }

    /**
     * 单独保存简历基本信息（更新简历主表的基本字段）
     */
    @PostMapping("/basic")
    public Result<Void> saveJobBasic(@Valid @RequestBody Map<String, Object> body) {
        Long userId = UserHolder.getUserId();
        if (body.get("jobType") instanceof String) {
            body.put("jobType", Constant.JobType.fromLabel((String) body.get("jobType")));
        }
        Resume resume = JSON.parseObject(JSON.toJSONString(body), Resume.class);
        resume.setUserId(userId);
        if (resume.getId() == null) {
            Resume existing = resumeMapper.getDefaultByuserId(userId);
            if (existing != null) {
                resume.setId(existing.getId());
            }
        } else {
            Resume existing = resumeMapper.getResumeById(resume.getId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                return Result.error("无权操作该简历");
            }
        }
        log.info("单独保存简历基本信息: id={}", resume.getId());
        resumeService.saveJobIntention(resume);
        return Result.success();
    }

    /**
     * 获取求职意向
     * @return
     */

    @GetMapping("/default")
    public Result getJobIntention() {
        Long userId = UserHolder.getUserId();
        return resumeService.getJobIntention(userId);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<ByteArrayResource> exportResumePdf(@PathVariable Long id) throws Exception {
        log.info("导出简历PDF: id={}", id);
        byte[] pdfBytes = resumeService.exportResumePdf(id);
        ResumeVO vo = resumeService.getResumeDetail(id);
        String fileName = (vo != null && vo.getName() != null ? vo.getName() : "resume") + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
    }
}
