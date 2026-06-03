package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Resume;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeEducation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeExperience;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeProject;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResumeQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SaveResumeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;

public interface ResumeService {

    PageResult listResumes(ResumeQueryDTO queryDTO);

    ResumeVO getResumeDetail(Long id);

    ResumeVO saveResume(SaveResumeDTO saveDTO);

    void deleteResume(Long id);

    Result<Void> setDefaultResume(Long id);

    SaveResumeDTO optimizeResume(SaveResumeDTO dto);

    // ==================== 单表操作 ====================
    /** 新增或更新单条教育经历（id为空则新增，不为空则更新） */
    ResumeEducation saveEducation(ResumeEducation education);
    /** 根据ID删除单条教育经历 */
    void deleteEducation(Long id);
    /** 新增或更新单条工作经历（id为空则新增，不为空则更新） */
    ResumeExperience saveExperience(ResumeExperience experience);
    /** 根据ID删除单条工作经历 */
    void deleteExperience(Long id);
    /** 新增或更新单条项目经历（id为空则新增，不为空则更新） */
    ResumeProject saveProject(ResumeProject project);
    /** 根据ID删除单条项目经历 */
    void deleteProject(Long id);
    /** 单独保存求职意向（更新简历主表的部分字段） */
    Result saveJobIntention(Resume resume);

    Result getCompanyApply(ApplicationQueryDTO applicationQueryDTO);

    Result createInterview(Interview interview,Byte status);

    /** 更新面试状态，当面试状态变为"面试成功"时自动将申请状态改为"已录用" */
    void updateInterviewStatus(Long interviewId, Long applicationId, Integer interviewStatus);


    //hr查看简历并修改申请状态
    Result<ResumeVO> getResumeDetail(Long id, Long applicationId);

    Result getJobIntention(Long userId);

    void saveDefaltResume(Resume resume);

    byte[] exportResumePdf(Long resumeId);
}
