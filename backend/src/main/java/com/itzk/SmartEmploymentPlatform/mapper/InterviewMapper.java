package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Interview;
import com.itzk.SmartEmploymentPlatform.pojo.vo.InterviewVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface InterviewMapper {
    void insert(Interview interview);

    List<InterviewVo> select(Interview interview);

    List<InterviewVo> getAllCompanyInterview(Interview interview);

    /** 更新面试状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Byte status);


    void updata(Interview view);

    @Select("select * from interview where id = #{id}")
    Interview getById(Long id);

    /** 统计本月面试安排数 */
    @Select("SELECT COUNT(*) FROM interview WHERE company_id = #{companyId} AND created_at >= #{monthStart}")
    int countByCompanyIdAndMonth(@Param("companyId") Long companyId, @Param("monthStart") java.time.LocalDateTime monthStart);

    @Select("select * from interview where user_id = #{userId} and job_id = #{jobId}")
    Interview getByUserIDAndJobId(Long userId, Long jobId);

    @Select("SELECT COUNT(*) FROM interview WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Select("SELECT * FROM interview WHERE application_id = #{applicationId} LIMIT 1")
    Interview getByApplicationId(@Param("applicationId") Long applicationId);

    /** 查询已过期但状态仍为未处理的面试（待确认/已接受/待定） */
    @Select("SELECT * FROM interview WHERE status IN (0, 1, 2) AND interview_time < NOW()")
    List<Interview> selectExpired();

    /** 将单条面试标记为已过期 */
    @
            Update("UPDATE interview SET status = 5, updated_at = NOW() WHERE id = #{id}")
    int expireById(@Param("id") Long id);
}
