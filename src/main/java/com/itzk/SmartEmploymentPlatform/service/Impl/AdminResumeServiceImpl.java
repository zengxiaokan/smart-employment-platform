package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Resume;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeEducation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeExperience;
import com.itzk.SmartEmploymentPlatform.pojo.entry.ResumeProject;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminResumeVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.service.AdminResumeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminResumeServiceImpl implements AdminResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public PageInfo<AdminResumeVO> list(int page, int size, String name, String jobIntention) {
        PageHelper.startPage(page, size);
        List<AdminResumeVO> list = resumeMapper.selectAdminList(name, jobIntention);
        return new PageInfo<>(list);
    }

    @Override
    public ResumeVO detail(Long id) {
        Resume r = resumeMapper.getResumeById(id);
        if (r == null) throw new BusinessException("简历不存在");

        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(r, vo);

        List<ResumeEducation> educations = resumeMapper.getEducationsByResumeId(id);
        List<ResumeExperience> experiences = resumeMapper.getExperiencesByResumeId(id);
        List<ResumeProject> projects = resumeMapper.getProjectsByResumeId(id);
        vo.setEducations(educations != null ? educations : List.of());
        vo.setExperiences(experiences != null ? experiences : List.of());
        vo.setProjects(projects != null ? projects : List.of());
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        resumeMapper.deleteResume(id);
    }
}
