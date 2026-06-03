package com.itzk.SmartEmploymentPlatform.mapper;

import com.github.pagehelper.Page;
import com.itzk.SmartEmploymentPlatform.pojo.entry.*;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryResumeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResumeQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeMapper {

    Page<Resume> listResumes(ResumeQueryDTO queryDTO);

    @Select("SELECT * FROM resumes")
    List<Resume> getAllResumes();

    /** 批量查简历，只查已投递该岗位的候选人 */
    List<Resume> getResumesByIds(@Param("ids") List<Long> ids);

    Resume getResumeById(@Param("id") Long id);

    int insertResume(Resume resume);

    int updateResume(Resume resume);

    int deleteResume(@Param("id") Long id);

    List<ResumeEducation> getEducationsByResumeId(@Param("resumeId") Long resumeId);

    /** 批量查教育经历 */
    List<ResumeEducation> getEducationsByResumeIds(@Param("resumeIds") List<Long> resumeIds);

    int batchInsertEducations(@Param("list") List<ResumeEducation> list);

    int deleteEducationsByResumeId(@Param("resumeId") Long resumeId);

    List<ResumeExperience> getExperiencesByResumeId(@Param("resumeId") Long resumeId);

    int batchInsertExperiences(@Param("list") List<ResumeExperience> list);

    int deleteExperiencesByResumeId(@Param("resumeId") Long resumeId);

    List<ResumeProject> getProjectsByResumeId(@Param("resumeId") Long resumeId);

    int batchInsertProjects(@Param("list") List<ResumeProject> list);

    int deleteProjectsByResumeId(@Param("resumeId") Long resumeId);

    // ==================== 单表操作：教育经历 ====================
    /** 新增单条教育经历 */
    int insertEducation(ResumeEducation education);
    /** 更新单条教育经历 */
    int updateEducation(ResumeEducation education);
    /** 根据ID删除单条教育经历 */
    int deleteEducationById(@Param("id") Long id);

    // ==================== 单表操作：工作经历 ====================
    /** 新增单条工作经历 */
    int insertExperience(ResumeExperience experience);
    /** 更新单条工作经历 */
    int updateExperience(ResumeExperience experience);
    /** 根据ID删除单条工作经历 */
    int deleteExperienceById(@Param("id") Long id);

    // ==================== 单表操作：项目经历 ====================
    /** 新增单条项目经历 */
    int insertProject(ResumeProject project);
    /** 更新单条项目经历 */
    int updateProject(ResumeProject project);
    /** 根据ID删除单条项目经历 */
    int deleteProjectById(@Param("id") Long id);

    @Select("select count(*) from resumes where user_id = #{userId}")
    int getByuserId(Long userId);

    @Update("update resumes set is_default = 0 where is_default = 1 and user_id = #{userId}")
    int cancelDefault(Long userId);

    @Update("update resumes set is_default = 1 where id = #{id}")
    void addDefault(Long id);

    @Select("select * from resumes where user_id = #{userId} and is_default = 1 limit 1")
    Resume getDefaultByuserId(Long userId);

    @Select("select * from resumes where id = #{resumeId}")
    void getById(Long resumeId);


    Page<Resume> getAllIntention(QueryResumeDTO dto);

    List<com.itzk.SmartEmploymentPlatform.pojo.vo.AdminResumeVO> selectAdminList(@Param("name") String name,
                                                                                   @Param("jobIntention") String jobIntention);
}
