package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.*;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResumeQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SaveResumeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.ai.capability.ResumeOptimizeCapability;
import com.itzk.SmartEmploymentPlatform.service.ResumeService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ResumeServiceImpl implements ResumeService {


    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private ResumeOptimizeCapability optimizeCapability;


    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private JobsMapper jobsMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageResult listResumes(ResumeQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        Page<Resume> page = resumeMapper.listResumes(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public ResumeVO getResumeDetail(Long id) {
        Resume resume = resumeMapper.getResumeById(id);
        if (resume == null) {
            return null;
        }
        return buildResumeVO(resume);
    }

    @Transactional
    @Override
    public ResumeVO saveResume(SaveResumeDTO saveDTO) {
        Resume resume = new Resume();
        BeanUtils.copyProperties(saveDTO, resume);

        if (saveDTO.getId() == null) {
            resumeMapper.insertResume(resume);
        } else {
            resumeMapper.updateResume(resume);
        }

        Long resumeId = resume.getId();
        replaceSubTables(resumeId, saveDTO.getEducations(), saveDTO.getExperiences(), saveDTO.getProjects());

        Resume savedResume = resumeMapper.getResumeById(resumeId);
        return buildResumeVO(savedResume);
    }

    @Transactional
    @Override
    public void deleteResume(Long id) {
        resumeMapper.deleteEducationsByResumeId(id);
        resumeMapper.deleteExperiencesByResumeId(id);
        resumeMapper.deleteProjectsByResumeId(id);
        resumeMapper.deleteResume(id);
    }

    @Override
    public Result<Void> setDefaultResume(Long id) {
        //将目标默认值取消
        //取消时只能取消自己的简历
        Long userId = UserHolder.getUserId();
        resumeMapper.cancelDefault(userId);
        //设置默认简历
        resumeMapper.addDefault(id);



        return Result.success();
    }

    @Override
    public SaveResumeDTO optimizeResume(SaveResumeDTO dto) {
        return optimizeCapability.optimize(dto);
    }

    // ==================== 单表操作 ====================

    /**
     * 新增或更新单条教育经历
     * @param education 教育经历对象，id为空则新增，不为空则更新
     */
    @Override
    public ResumeEducation saveEducation(ResumeEducation education) {
        if (education.getId() == null) {
            resumeMapper.insertEducation(education);
        } else {
            resumeMapper.updateEducation(education);
        }
        return education;
    }

    /**
     * 根据ID删除单条教育经历
     */
    @Override
    public void deleteEducation(Long id) {
        resumeMapper.deleteEducationById(id);
    }

    /**
     * 新增或更新单条工作经历
     * @param experience 工作经历对象，id为空则新增，不为空则更新
     */
    @Override
    public ResumeExperience saveExperience(ResumeExperience experience) {
        if (experience.getId() == null) {
            resumeMapper.insertExperience(experience);
        } else {
            resumeMapper.updateExperience(experience);
        }
        return experience;
    }

    /**
     * 根据ID删除单条工作经历
     */
    @Override
    public void deleteExperience(Long id) {
        resumeMapper.deleteExperienceById(id);
    }

    /**
     * 新增或更新单条项目经历
     * @param project 项目经历对象，id为空则新增，不为空则更新
     */
    @Override
    public ResumeProject saveProject(ResumeProject project) {
        if (project.getId() == null) {
            resumeMapper.insertProject(project);
        } else {
            resumeMapper.updateProject(project);
        }
        return project;
    }

    /**
     * 根据ID删除单条项目经历
     */
    @Override
    public void deleteProject(Long id) {
        resumeMapper.deleteProjectById(id);
    }

    /**
     * 单独保存求职意向，更新简历主表的求职意向相关字段
     */
    @Override
    public Result saveJobIntention(Resume resume) {
        if (resume.getId() == null) {
            // 前端没传 id，查一下用户是否已有默认简历，有就更新、没有就新增
            Resume existing = resumeMapper.getDefaultByuserId(resume.getUserId());
            if (existing != null) {
                resume.setId(existing.getId());
            } else {
                resume.setResumeName("默认简历");
                resume.setIsDefault((byte) 1);
                resumeMapper.insertResume(resume);
                return Result.success();
            }
        }
        resumeMapper.updateResume(resume);
        return Result.success();
    }

    @Override
    public Result getCompanyApply(ApplicationQueryDTO applicationQueryDTO) {


        //通过公司id拿到公司所有投递情况
        List<ApplicationHrVO> applications = applicationMapper.getCompanyApply(applicationQueryDTO);
        if(applications == null || applications.isEmpty()) {
            return Result.error("暂时没有人投递贵公司");
        }



        return Result.success(applications);
    }

    @Override
    @Transactional
    public Result createInterview(Interview interview, Byte status) {
        // 面试时间不能早于当前时间
        if (interview.getInterviewTime() != null && interview.getInterviewTime().isBefore(LocalDateTime.now())) {
            return Result.error("面试时间不能早于当前时间");
        }
        // 二次投递场景：同一申请可能已有面试记录，有则更新、无则插入
        Interview existing = interviewMapper.getByApplicationId(interview.getApplicationId());
        if (existing != null) {
            interview.setId(existing.getId());
            interviewMapper.updata(interview);
        } else {
            interviewMapper.insert(interview);
        }
        Application a = new Application();
        a.setId(interview.getApplicationId());
        a.setStatus(status);
        a.setInterviewTime(interview.getInterviewTime());
        applicationMapper.updata(a);
        return Result.success();
    }

    @Override
    @Transactional
    public void updateInterviewStatus(Long interviewId, Long applicationId, Integer interviewStatus) {
        // 更新面试状态
        interviewMapper.updateStatus(interviewId, interviewStatus.byteValue());
        // 面试成功 → 申请状态变为"已录用"
        if (interviewStatus != null && interviewStatus == Constant.InterviewStatus.PASSED) {
            Application a = new Application();
            a.setId(applicationId);
            a.setStatus(Constant.ApplicationStatus.Hired);
            applicationMapper.updata(a);
            try { redisTemplate.delete("home:stats"); } catch (Exception ignored) {}
            // 减少岗位剩余招聘人数
            Application app = applicationMapper.getById(applicationId);
            if (app != null && app.getJobId() != null) {
                jobsMapper.decrementHasCount(app.getJobId());
                Job job = jobsMapper.getCompanyIdByjobId(app.getJobId());
                if (job != null && job.getCompanyId() != null) {
                    companyMapper.incrementJobConfirm(job.getCompanyId());
                    if (job.getHasCount() <= 0) {
                        jobsMapper.forceOffline(app.getJobId());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public Result<ResumeVO> getResumeDetail(Long id, Long applicationId) {

        Application a = applicationMapper.getById(applicationId);
        byte status =a.getStatus();
        if( status== Constant.ApplicationStatus.PENDING) {
            status = Constant.ApplicationStatus.VIEWED;
        }

        applicationMapper.setStatusById(applicationId,status);
        ResumeVO vo = this.getResumeDetail(id);
        return Result.success(vo);
    }

    /**
     * 获取求职意向，即默认简历表
     * @param userId
     * @return
     */
    @Override
    public Result getJobIntention(Long userId) {
        Resume resume = resumeMapper.getDefaultByuserId(userId);

        return Result.success(resume);
    }

    /**
     * 保存默认简历（存在则更新，不存在则新增）
     * @param resume
     */
    @Override
    public void saveDefaltResume(Resume resume) {
        Resume existing = resumeMapper.getDefaultByuserId(resume.getUserId());
        if (existing != null) {
            resume.setId(existing.getId());
            resumeMapper.updateResume(resume);
        } else {
            resumeMapper.insertResume(resume);
        }
    }


    private ResumeVO buildResumeVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo);

        Long resumeId = resume.getId();
        vo.setEducations(resumeMapper.getEducationsByResumeId(resumeId));
        vo.setExperiences(resumeMapper.getExperiencesByResumeId(resumeId));
        vo.setProjects(resumeMapper.getProjectsByResumeId(resumeId));
        return vo;
    }

    private void replaceSubTables(Long resumeId,
                                  List<ResumeEducation> educations,
                                  List<ResumeExperience> experiences,
                                  List<ResumeProject> projects)
    {


        if (!CollectionUtils.isEmpty(educations)) {
            resumeMapper.deleteEducationsByResumeId(resumeId);
            for (ResumeEducation e : educations) {
                e.setResumeId(resumeId);
            }
            resumeMapper.batchInsertEducations(educations);
        }
        if (!CollectionUtils.isEmpty(experiences)) {
            resumeMapper.deleteExperiencesByResumeId(resumeId);
            for (ResumeExperience e : experiences) {
                e.setResumeId(resumeId);
            }
            resumeMapper.batchInsertExperiences(experiences);
        }
        if (!CollectionUtils.isEmpty(projects)) {
            resumeMapper.deleteProjectsByResumeId(resumeId);
            for (ResumeProject p : projects) {
                p.setResumeId(resumeId);
            }
            resumeMapper.batchInsertProjects(projects);
        }
    }

    @Override
    public byte[] exportResumePdf(Long resumeId) {
        ResumeVO vo = getResumeDetail(resumeId);
        if (vo == null) {
            throw new RuntimeException("简历不存在");
        }
        // 下载头像并转 base64 data URI 嵌入 HTML
        String avatarImgPath = null;
        if (notEmpty(vo.getCharacterAvatar())) {
            try {
                byte[] imgBytes = new java.net.URL(vo.getCharacterAvatar()).openStream().readAllBytes();
                String b64 = java.util.Base64.getEncoder().encodeToString(imgBytes);
                avatarImgPath = "data:image/jpeg;base64," + b64;
                log.info("PDF头像已下载: {} bytes → base64 len={}", imgBytes.length, b64.length());
            } catch (Exception e) {
                log.warn("PDF头像下载失败, url={}: {}", vo.getCharacterAvatar(), e.getMessage());
            }
        }
        String html = buildResumeHtml(vo, avatarImgPath);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(os);

            // 读取字体文件为字节数组，避免 PDFBox 系统字体扫描的影响
            byte[] fontBytes = null;
            String[] fontPaths = {
                    "C:/Windows/Fonts/simhei.ttf",
                    "C:/Windows/Fonts/msyh.ttc",
                    "C:/Windows/Fonts/simsun.ttc",
                    "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                    "/System/Library/Fonts/PingFang.ttc",
            };
            for (String p : fontPaths) {
                File f = new File(p);
                if (f.exists()) {
                    fontBytes = java.nio.file.Files.readAllBytes(f.toPath());
                    log.info("PDF导出使用字体: {} ({} bytes)", p, fontBytes.length);
                    break;
                }
            }
            if (fontBytes == null) {
                // 尝试 classpath 内置字体
                java.io.InputStream is = getClass().getClassLoader()
                        .getResourceAsStream("fonts/NotoSansSC-Regular.ttf");
                if (is != null) {
                    fontBytes = is.readAllBytes();
                    is.close();
                    log.info("PDF导出使用内置字体: {} bytes", fontBytes.length);
                }
            }
            if (fontBytes != null) {
                byte[] finalFontBytes = fontBytes;
                builder.useFont(() -> new java.io.ByteArrayInputStream(finalFontBytes), "chinese");
            } else {
                log.warn("PDF导出未找到任何中文字体");
            }

            builder.run();
            byte[] result = os.toByteArray();
            log.info("PDF导出成功, resumeId={}, size={} bytes", resumeId, result.length);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出PDF失败, resumeId={}", resumeId, e);
            throw new RuntimeException("导出PDF失败: " + e.getMessage());
        }
    }

    private String buildResumeHtml(ResumeVO vo, String avatarImgPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>");
        sb.append("@page{size:A4;margin:8mm}*{margin:0;padding:0}");
        sb.append("body{font-family:'chinese',sans-serif;font-size:13px;color:#475569;line-height:1.7}");
        sb.append(".wrap{display:table;width:100%;min-height:297mm}");
        // 左侧栏 — 浅灰白，与右侧纯白形成微妙界限
        sb.append(".side{display:table-cell;width:30%;background:#f8fafc;color:#475569;padding:28px 18px;vertical-align:top;border-right:1px solid #e2e8f0}");
        sb.append(".side .avatar{width:90px;height:90px;background:#ecfdf5;margin:0 auto 16px;text-align:center;line-height:90px;font-size:36px;color:#059669;border:3px solid #a7f3d0}");
        sb.append(".side h1{font-size:22px;text-align:center;color:#0f172a;margin-bottom:2px}");
        sb.append(".side .job{font-size:12px;color:#059669;text-align:center;margin-bottom:22px;font-weight:600}");
        sb.append(".side .sec{margin-bottom:18px}");
        sb.append(".side .sec h3{font-size:11px;letter-spacing:2px;color:#059669;border-bottom:1px solid #e2e8f0;padding-bottom:5px;margin-bottom:8px}");
        sb.append(".side .info{font-size:11px;color:#475569;margin-bottom:5px}");
        sb.append(".side .tag{display:inline;background:#fff;color:#059669;padding:3px 8px;margin:2px 3px 2px 0;font-size:10px;line-height:2.2;border:1px solid #a7f3d0}");
        // 右侧主内容
        sb.append(".main{display:table-cell;width:70%;padding:28px 24px;vertical-align:top}");
        sb.append(".main .sec{margin-bottom:20px}");
        sb.append(".main .sec h3{font-size:15px;color:#059669;border-bottom:2px solid #a7f3d0;padding-bottom:5px;margin-bottom:10px;letter-spacing:1px}");
        sb.append(".main .self{font-size:12.5px;color:#475569;line-height:1.7}");
        sb.append(".main .item{margin-bottom:12px}");
        sb.append(".main .item-row{overflow:hidden;margin-bottom:2px}");
        sb.append(".main .item-title{float:left;font-weight:700;font-size:13px;color:#0f172a}");
        sb.append(".main .item-sub{clear:both;color:#64748b;font-size:11px}");
        sb.append(".main .item-date{float:right;color:#94a3b8;font-size:11px;white-space:nowrap}");
        sb.append(".main .item-desc{clear:both;color:#64748b;font-size:12px;margin-top:3px}");
        sb.append(".main .skill-tag{display:inline;background:#ecfdf5;color:#059669;padding:3px 10px;margin:2px 4px 2px 0;font-size:11px;line-height:2.2;border:1px solid #a7f3d0}");
        sb.append("</style></head><body><div class=\"wrap\">");

        // 左侧栏
        sb.append("<div class=\"side\">");
        if (avatarImgPath != null) {
            sb.append("<div class=\"avatar\"><img src=\"").append(avatarImgPath)
              .append("\" width=\"90\" height=\"90\" style=\"width:90px;height:90px\"/></div>");
        } else {
            sb.append("<div class=\"avatar\">").append(emptyToDash(vo.getName())).append("</div>");
        }
        sb.append("<h1>").append(emptyToDash(vo.getName())).append("</h1>");
        sb.append("<div class=\"job\">").append(emptyToDash(vo.getJobIntention())).append("</div>");

        sb.append("<div class=\"sec\"><h3>基本信息</h3>");
        sb.append("<div class=\"info\">").append(vo.getGender() != null ? (vo.getGender() == 1 ? "男" : "女") : "-").append(" | ").append(vo.getAge() != null ? vo.getAge() + "岁" : "-").append("</div>");
        sb.append("<div class=\"info\">").append(emptyToDash(vo.getPhone())).append("</div>");
        sb.append("<div class=\"info\">").append(emptyToDash(vo.getEmail())).append("</div>");
        if (notEmpty(vo.getCity())) {
            sb.append("<div class=\"info\">").append(vo.getCity()).append("</div>");
        }
        sb.append("</div>");

        if (notEmpty(vo.getSkills())) {
            sb.append("<div class=\"sec\"><h3>技能标签</h3>");
            String[] skills = vo.getSkills().split(",");
            for (String s : skills) {
                String t = s.trim();
                if (!t.isEmpty()) sb.append("<span class=\"tag\">").append(t).append("</span>");
            }
            sb.append("</div>");
        }
        sb.append("</div>");

        // 右侧主内容
        sb.append("<div class=\"main\">");

        if (notEmpty(vo.getJobIntention()) || vo.getSalaryMin() != null || vo.getSalaryMax() != null) {
            sb.append("<div class=\"sec\"><h3>求职意向</h3>");
            StringBuilder line = new StringBuilder();
            if (notEmpty(vo.getJobIntention())) line.append(vo.getJobIntention());
            if (notEmpty(vo.getCity())) line.append(" | ").append(vo.getCity());
            if (vo.getSalaryMin() != null || vo.getSalaryMax() != null) {
                line.append(" | ");
                line.append(vo.getSalaryMin() != null ? Math.round(vo.getSalaryMin() / 1000f) + "K" : "?");
                line.append(" - ");
                line.append(vo.getSalaryMax() != null ? Math.round(vo.getSalaryMax() / 1000f) + "K" : "?");
            }
            sb.append("<div class=\"self\">").append(line).append("</div>");
            sb.append("</div>");
        }

        if (notEmpty(vo.getSelfDescription())) {
            sb.append("<div class=\"sec\"><h3>自我描述</h3>");
            sb.append("<div class=\"self\">").append(vo.getSelfDescription()).append("</div>");
            sb.append("</div>");
        }

        if (vo.getEducations() != null && !vo.getEducations().isEmpty()) {
            sb.append("<div class=\"sec\"><h3>教育经历</h3>");
            for (ResumeEducation e : vo.getEducations()) {
                sb.append("<div class=\"item\">");
                sb.append("<div class=\"item-row\">");
                sb.append("<span class=\"item-title\">").append(emptyToDash(e.getSchool())).append("</span>");
                sb.append("<span class=\"item-date\">").append(fmtDate(e.getStartTime())).append(" - ").append(fmtDate(e.getEndTime())).append("</span>");
                sb.append("</div>");
                sb.append("<div class=\"item-sub\">").append(emptyToDash(e.getMajor())).append(" | ").append(eduLabel(e.getEducation())).append("</div>");
                if (notEmpty(e.getDescription())) {
                    sb.append("<div class=\"item-desc\">").append(e.getDescription()).append("</div>");
                }
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        if (vo.getExperiences() != null && !vo.getExperiences().isEmpty()) {
            sb.append("<div class=\"sec\"><h3>工作经历</h3>");
            for (ResumeExperience e : vo.getExperiences()) {
                sb.append("<div class=\"item\">");
                sb.append("<div class=\"item-row\">");
                sb.append("<span class=\"item-title\">").append(emptyToDash(e.getCompany())).append("</span>");
                sb.append("<span class=\"item-date\">").append(fmtDate(e.getStartTime())).append(" - ").append(fmtDate(e.getEndTime())).append("</span>");
                sb.append("</div>");
                sb.append("<div class=\"item-sub\">").append(emptyToDash(e.getPosition())).append("</div>");
                if (notEmpty(e.getDescription())) {
                    sb.append("<div class=\"item-desc\">").append(e.getDescription()).append("</div>");
                }
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        if (vo.getProjects() != null && !vo.getProjects().isEmpty()) {
            sb.append("<div class=\"sec\"><h3>项目经历</h3>");
            for (ResumeProject p : vo.getProjects()) {
                sb.append("<div class=\"item\">");
                sb.append("<div class=\"item-row\">");
                sb.append("<span class=\"item-title\">").append(emptyToDash(p.getName())).append("</span>");
                sb.append("<span class=\"item-date\">").append(fmtDate(p.getStartTime())).append(" - ").append(fmtDate(p.getEndTime())).append("</span>");
                sb.append("</div>");
                sb.append("<div class=\"item-sub\">").append(emptyToDash(p.getRole())).append("</div>");
                if (notEmpty(p.getDescription())) {
                    sb.append("<div class=\"item-desc\">").append(p.getDescription()).append("</div>");
                }
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM");

    private String fmtDate(java.time.LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "";
    }

    private String emptyToDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    private boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private String eduLabel(Byte edu) {
        if (edu == null) return "-";
        switch (edu) {
            case 0: return "初中";
            case 1: return "高中";
            case 2: return "中专";
            case 3: return "大专";
            case 4: return "本科";
            case 5: return "硕士";
            case 6: return "博士";
            default: return "-";
        }
    }
}
