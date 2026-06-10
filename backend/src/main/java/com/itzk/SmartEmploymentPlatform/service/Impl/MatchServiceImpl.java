package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.itzk.SmartEmploymentPlatform.ai.capability.MatchSummaryCapability;
import com.itzk.SmartEmploymentPlatform.mapper.*;
import com.itzk.SmartEmploymentPlatform.pojo.entry.*;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchDetailVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SkillDetailVO;
import com.itzk.SmartEmploymentPlatform.service.MatchService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI匹配核心实现
 * 流程：岗位列表（按投递数排序）→ 点岗位 → 智能评分 → AI分析
 * 计分算法：技能30分 + 学历30分 + 经验25分 + 薪资15分 = 满分100
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private JobsMapper jobsMapper;
    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private MatchRecordMapper matchRecordMapper;
    @Autowired
    private MatchSkillDetailMapper matchSkillDetailMapper;
    @Autowired
    private MatchSummaryCapability matchSummaryCapability;

    @Override
    public List<Job> listCompanyJobs(Long companyId) {
        return jobsMapper.getActiveJobsByCompanyId(companyId);
    }

    /** 对单个岗位执行评分，不生成AI评语（点详情时按需生成） */
    private int matchOneJob(Job job) {
        List<Long> appliedResumeIds = applicationMapper.getResumeIdsByJobId(job.getId());
        log.info("已投递简历ID: jobId={}, resumeIds={}", job.getId(), appliedResumeIds);
        if (appliedResumeIds.isEmpty()) {
            return 0;
        }
        List<Resume> appliedResumes = resumeMapper.getResumesByIds(appliedResumeIds);
        log.info("查询到简历数: {}", appliedResumes.size());

        Set<String> jobSkills = parseSkills(job.getJobSkills());
        boolean jobHasSkills = !jobSkills.isEmpty();
        int jobMinYears = parseExperienceYears(job.getExperience());

        int matchCount = 0;
        for (Resume resume : appliedResumes) {
            Set<String> resumeSkills = parseSkills(resume.getSkills());

            BigDecimal skillScore;
            List<MatchSkillDetail> skillDetails;

            if (jobHasSkills && !resumeSkills.isEmpty()) {
                Set<String> intersection = new HashSet<>(jobSkills);
                intersection.retainAll(resumeSkills);
                skillScore = BigDecimal.valueOf(intersection.size())
                        .divide(BigDecimal.valueOf(jobSkills.size()), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(30))
                        .max(BigDecimal.valueOf(3));
                skillDetails = buildSkillDetails(jobSkills, resumeSkills);
            } else {
                skillScore = BigDecimal.valueOf(3);
                skillDetails = Collections.emptyList();
            }

            BigDecimal experienceScore = calcExperienceScore(resume, jobMinYears)
                    .max(BigDecimal.valueOf(2));
            BigDecimal educationScore = calcEducationScore(resume, job.getEducation())
                    .max(BigDecimal.valueOf(3));
            BigDecimal salaryScore = calcSalaryScore(resume, job)
                    .max(BigDecimal.valueOf(2));
            BigDecimal totalScore = skillScore.add(experienceScore)
                    .add(educationScore).add(salaryScore);

            MatchRecord record = new MatchRecord();
            record.setJobId(job.getId());
            record.setResumeId(resume.getId());
            record.setTotalScore(totalScore);
            record.setSkillScore(skillScore);
            record.setExperienceScore(experienceScore);
            record.setEducationScore(educationScore);
            record.setSalaryScore(salaryScore);
            record.setStatus(0);
            matchRecordMapper.insert(record);

            if (!skillDetails.isEmpty()) {
                for (MatchSkillDetail detail : skillDetails) {
                    detail.setMatchId(record.getId());
                }
                matchSkillDetailMapper.batchInsert(skillDetails);
            }

            matchCount++;
        }
        return matchCount;
    }

    @Override
    public List<MatchVO> listByJobId(Long jobId) {
        // 校验岗位归属
        Long companyId = UserHolder.getCompanyId();
        Job job = jobsMapper.getCompanyIdByjobId(jobId);
        if (job == null || !job.getCompanyId().equals(companyId)) {
            return Collections.emptyList();  // 不是本公司的岗位，返回空列表
        }
        // 首次访问：执行评分入库；再次访问：直接返回库数据
        List<MatchRecord> existing = matchRecordMapper.selectByJobId(jobId);
        if (existing.isEmpty()) {
            matchOneJob(job);
        }

        List<MatchVO> list = matchRecordMapper.selectVOByJobId(jobId);
        if (list.isEmpty()) {
            return list;
        }

        // 批量查技能明细（仅限已评分的记录）
        List<Long> matchIds = list.stream()
                .map(MatchVO::getMatchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, List<MatchSkillDetail>> detailMap = Collections.emptyMap();
        if (!matchIds.isEmpty()) {
            List<MatchSkillDetail> allDetails = matchSkillDetailMapper.selectByMatchIds(matchIds);
            detailMap = allDetails.stream()
                    .collect(Collectors.groupingBy(MatchSkillDetail::getMatchId));
        }

        for (MatchVO vo : list) {
            vo.setEducation(vo.getMaxEducation() != null ? Constant.Education.label(vo.getMaxEducation()) : "不限");

            if (vo.getMatchId() != null) {
                List<MatchSkillDetail> details = detailMap.getOrDefault(vo.getMatchId(), Collections.emptyList());
                String matchedSkills = details.stream()
                        .filter(d -> d.getMatched() == 1)
                        .map(MatchSkillDetail::getSkillName)
                        .collect(Collectors.joining("、"));
                vo.setMatchedSkills(matchedSkills);
            } else {
                vo.setMatchedSkills("");
            }
        }
        return list;
    }

    @Override
    @Transactional
    public MatchDetailVO getDetail(Long matchId) {
        MatchRecord record = matchRecordMapper.selectById(matchId);
        if (record == null) {
            throw new RuntimeException("匹配记录不存在");
        }

        Resume resume = resumeMapper.getResumeById(record.getResumeId());
        Job job = jobsMapper.getCompanyIdByjobId(record.getJobId());
        Long companyId = UserHolder.getCompanyId();
        if (job == null || !job.getCompanyId().equals(companyId)) {
            throw new RuntimeException("无权查看该匹配记录");
        }

        MatchDetailVO vo = new MatchDetailVO();
        vo.setMatchId(record.getId());
        vo.setJobId(record.getJobId());
        vo.setJobTitle(job != null ? job.getTitle() : null);
        vo.setResumeId(record.getResumeId());
        vo.setName(resume != null ? resume.getName() : null);
        vo.setAvatar(resume != null ? resume.getCharacterAvatar() : null);
        vo.setTotalScore(record.getTotalScore());
        vo.setSkillScore(record.getSkillScore());
        vo.setExperienceScore(record.getExperienceScore());
        vo.setEducationScore(record.getEducationScore());
        vo.setSalaryScore(record.getSalaryScore());
        vo.setMatchSummary(record.getMatchSummary());
        vo.setStatus(record.getStatus());

        // 最高学历（直接读冗余字段）
        vo.setEducation(resume.getMaxEducation() != null
                ? Constant.Education.label(resume.getMaxEducation()) : "不限");

        List<MatchSkillDetail> details = matchSkillDetailMapper.selectByMatchId(matchId);
        List<SkillDetailVO> skillVOs = new ArrayList<>();
        for (MatchSkillDetail d : details) {
            SkillDetailVO s = new SkillDetailVO();
            s.setSkillName(d.getSkillName());
            s.setMatched(d.getMatched());
            skillVOs.add(s);
        }
        vo.setSkillDetails(skillVOs);

        return vo;
    }

    @Override
    public String regenerateSummary(Long matchId) {
        MatchRecord record = matchRecordMapper.selectById(matchId);
        if (record == null) {
            throw new RuntimeException("匹配记录不存在");
        }
        String summary = generateSummary(record);
        doUpdateSummary(matchId, summary);
        log.info("评语重新生成完成: matchId={}", matchId);
        return summary;
    }

    @Transactional
    public void doUpdateSummary(Long matchId, String summary) {
        matchRecordMapper.updateSummary(matchId, summary);
    }

    /**
     * 匹配阶段直接生成AI评语（使用内存数据，避免二次查库）
     */
    private String generateSummaryInMatch(Job job, Resume resume, BigDecimal totalScore,
                                          BigDecimal skillScore, BigDecimal experienceScore,
                                          BigDecimal educationScore, BigDecimal salaryScore,
                                          List<String> matchedSkills) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("jobTitle", job.getTitle() != null ? job.getTitle() : "");
            data.put("candidateName", resume.getName() != null ? resume.getName() : "");
            data.put("totalScore", totalScore);
            data.put("skillScore", skillScore);
            data.put("experienceScore", experienceScore);
            data.put("educationScore", educationScore);
            data.put("salaryScore", salaryScore);
            data.put("matchedSkills", matchedSkills);
            data.put("matchCount", matchedSkills.size());
            data.put("education", resume.getMaxEducation() != null
                    ? Constant.Education.label(resume.getMaxEducation()) : "不限");
            return matchSummaryCapability.generate(data);
        } catch (Exception e) {
            return "该候选人与岗位需求高度匹配，建议进一步沟通";
        }
    }

    // ==================== 评分算法 ====================

    private Set<String> parseSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private List<MatchSkillDetail> buildSkillDetails(Set<String> jobSkills, Set<String> resumeSkills) {
        List<MatchSkillDetail> list = new ArrayList<>();
        for (String skill : jobSkills) {
            MatchSkillDetail detail = new MatchSkillDetail();
            detail.setSkillName(skill);
            detail.setMatched(resumeSkills.contains(skill) ? 1 : 0);
            list.add(detail);
        }
        return list;
    }

    private int parseExperienceYears(String experience) {
        if (experience == null || experience.isEmpty() || experience.contains("不限")) {
            return 0;
        }
        if (experience.contains("应届")) {
            return 0;
        }
        String digits = experience.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 0;
        }
        if (experience.contains("以上")) {
            return Integer.parseInt(digits);
        }
        // "1-3年" → 取左值
        String[] parts = experience.split("-");
        if (parts.length > 0) {
            String left = parts[0].replaceAll("\\D", "");
            return left.isEmpty() ? 0 : Integer.parseInt(left);
        }
        return 0;
    }

    /**
     * 经验评分（渐进式）
     * min(候选人年限 / 岗位要求年限, 1) × 25
     * 例如：要求5年，候选人3年 → 3/5×25=15分
     */
    private BigDecimal calcExperienceScore(Resume resume, int jobMinYears) {
        int totalYears = resume.getTotalWorkYears() != null ? resume.getTotalWorkYears() : 0;

        if (jobMinYears == 0) {
            return BigDecimal.valueOf(25);
        }
        double ratio = Math.min((double) totalYears / jobMinYears, 1.0);
        return BigDecimal.valueOf(ratio * 25).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 学历评分
     * 岗位表 education: 0=不限 1=初中...7=博士（8 级）
     * 简历表 maxEducation: 0=初中 1=高中...6=博士（7 级）
     * 岗位值比简历值偏移 +1，比较时需对齐
     */
    private BigDecimal calcEducationScore(Resume resume, Byte jobEducation) {
        if (jobEducation == null || jobEducation == 0) {
            return BigDecimal.valueOf(30);
        }
        int maxEdu = resume.getMaxEducation() != null ? resume.getMaxEducation() : 0;
        // jobEducation 1-7 对应 resume maxEducation 0-6，偏移量 -1
        return maxEdu >= (jobEducation - 1) ? BigDecimal.valueOf(30) : BigDecimal.ZERO;
    }

    private BigDecimal calcSalaryScore(Resume resume, Job job) {
        if (resume.getSalaryMin() == null || job.getSalaryMax() == null) {
            return BigDecimal.valueOf(15);
        }
        return resume.getSalaryMin() <= job.getSalaryMax()
                ? BigDecimal.valueOf(15) : BigDecimal.ZERO;
    }

    // ==================== AI评语 ====================

    private String generateSummary(MatchRecord record) {
        try {
            Resume resume = resumeMapper.getResumeById(record.getResumeId());
            Job job = jobsMapper.getCompanyIdByjobId(record.getJobId());
            List<MatchSkillDetail> details = matchSkillDetailMapper.selectByMatchId(record.getId());
            List<String> matchedSkills = details.stream()
                    .filter(d -> d.getMatched() == 1)
                    .map(MatchSkillDetail::getSkillName)
                    .collect(Collectors.toList());
            return generateSummaryInMatch(job, resume, record.getTotalScore(),
                    record.getSkillScore(), record.getExperienceScore(),
                    record.getEducationScore(), record.getSalaryScore(), matchedSkills);
        } catch (Exception e) {
            return "该候选人与岗位需求高度匹配，建议进一步沟通";
        }
    }
}
