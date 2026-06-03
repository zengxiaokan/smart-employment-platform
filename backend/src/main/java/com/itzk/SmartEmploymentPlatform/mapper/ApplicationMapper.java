package com.itzk.SmartEmploymentPlatform.mapper;

import com.github.pagehelper.Page;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Application;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleApplicationVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApplicationMapper {


    void insertApply(Application application);


    //查询当前用户的所有职位申请
    List<SimpleApplicationVo> getByUserId(Long userId);

    @Update("update applications set status = #{status}, applied_at = now(), updated_at = now(), interview_time = null where id = #{applicationId} and user_id = #{userId}")
    int updataById(long userId, Long applicationId,byte status);

    ApplicationDitailVo getJobAndCompanyInfo(Long applicationId);

    @Select("select * from applications where company_id = #{companyId}")
    List<Application> getByCompanyId(Long companyId);

    List<ApplicationHrVO> getCompanyApply(@Param("query") ApplicationQueryDTO applicationQueryDTO);

    @Update("update applications set status = #{status} where id = #{applicationId}")
    void setStatusById(Long applicationId,byte status);

    void updata(Application application);

    @Select("select * from applications where id = #{applicationId}")
    Application getById(Long applicationId);

    /** 统计本月收到简历数 */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId} AND applied_at >= #{monthStart}")
    int countByCompanyIdAndMonth(@Param("companyId") Long companyId, @Param("monthStart") java.time.LocalDateTime monthStart);

    /** 统计本月入职数（状态变为已录用） */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId} AND status = 5 AND updated_at >= #{monthStart}")
    int countHiredByCompanyIdAndMonth(@Param("companyId") Long companyId, @Param("monthStart") java.time.LocalDateTime monthStart);

    /** 获取某岗位已投递的简历ID列表 */
    @Select("SELECT resume_id FROM applications WHERE job_id = #{jobId}")
    List<Long> getResumeIdsByJobId(Long jobId);

    @Select("SELECT COUNT(*) FROM applications WHERE status = 5")
    Long countHired();

    @Select("SELECT COUNT(*) FROM applications WHERE MONTH(applied_at) = MONTH(CURDATE()) AND YEAR(applied_at) = YEAR(CURDATE())")
    Long countByCurrentMonth();

    @Select("SELECT DATE(applied_at) AS date, COUNT(*) AS count FROM applications WHERE applied_at >= #{since} GROUP BY DATE(applied_at) ORDER BY date")
    List<Map<String, Object>> countNewByDate(@Param("since") java.time.LocalDateTime since);

    List<com.itzk.SmartEmploymentPlatform.pojo.vo.AdminApplicationVO> selectAdminList(@Param("applicantName") String applicantName,
                                                                                        @Param("jobTitle") String jobTitle,
                                                                                        @Param("status") Integer status);

    @Delete("DELETE FROM applications WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
