package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.vo.AnalyticsFunnelItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyticsMapper {

    /** 日期范围内的职位总数 */
    @Select("SELECT COUNT(*) FROM jobs WHERE company_id = #{companyId} AND created_at >= #{start} AND created_at <= #{end}")
    int countJobs(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 累计投递数（全量） */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId}")
    int countApplicationsAll(@Param("companyId") Long companyId);

    /** 累计面试数（全量） */
    @Select("SELECT COUNT(DISTINCT a.id) FROM applications a INNER JOIN interview i ON a.id = i.application_id WHERE a.company_id = #{companyId}")
    int countInterviewedAll(@Param("companyId") Long companyId);

    /** 累计入职数（全量） */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId} AND status = 5")
    int countHiredAll(@Param("companyId") Long companyId);

    /** 当前在招职位数 */
    @Select("SELECT COUNT(*) FROM jobs WHERE company_id = #{companyId} AND status = 1")
    int countActiveJobs(@Param("companyId") Long companyId);

    /** 日期范围内的投递总数 */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId} AND applied_at >= #{start} AND applied_at <= #{end}")
    int countApplications(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 日期范围内进入面试环节的申请数 */
    @Select("SELECT COUNT(DISTINCT a.id) FROM applications a INNER JOIN interview i ON a.id = i.application_id WHERE a.company_id = #{companyId} AND a.applied_at >= #{start} AND a.applied_at <= #{end}")
    int countInterviewed(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 日期范围内已录用的申请数 */
    @Select("SELECT COUNT(*) FROM applications WHERE company_id = #{companyId} AND status = 5 AND updated_at >= #{start} AND updated_at <= #{end}")
    int countHired(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 每日投递趋势 */
    @Select("SELECT DATE(applied_at) as dt, COUNT(*) as cnt FROM applications WHERE company_id = #{companyId} AND applied_at >= #{start} AND applied_at <= #{end} GROUP BY DATE(applied_at) ORDER BY dt")
    List<Map<String, Object>> dailyApplications(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 每日面试趋势 */
    @Select("SELECT DATE(i.created_at) as dt, COUNT(*) as cnt FROM interview i WHERE i.company_id = #{companyId} AND i.created_at >= #{start} AND i.created_at <= #{end} GROUP BY DATE(i.created_at) ORDER BY dt")
    List<Map<String, Object>> dailyInterviews(@Param("companyId") Long companyId, @Param("start") String start, @Param("end") String end);

    /** 按岗位统计转化漏斗（全部累计） */
    @Select("SELECT j.title as position, COUNT(a.id) as applications, " +
            "COUNT(DISTINCT i.id) as interviews, " +
            "SUM(CASE WHEN a.status = 5 THEN 1 ELSE 0 END) as hired " +
            "FROM applications a " +
            "LEFT JOIN jobs j ON a.job_id = j.id " +
            "LEFT JOIN interview i ON a.id = i.application_id " +
            "WHERE a.company_id = #{companyId} " +
            "GROUP BY j.id, j.title " +
            "ORDER BY applications DESC")
    List<AnalyticsFunnelItem> funnel(@Param("companyId") Long companyId);
}
