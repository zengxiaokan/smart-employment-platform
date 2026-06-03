package com.itzk.SmartEmploymentPlatform.mapper;

import com.github.pagehelper.Page;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminJobVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface JobsMapper {


    Page<Job> listJobs(JobQueryDTO jobQueryDTO);

    //查看公司是否真实存在，通过工作id查询公司id
    Job getCompanyIdByjobId(Long jobId);

    //根据职位id增加浏览数量
    @Update("update jobs set view_count = view_count + 1 where id = #{jobId}")
    int addViewCountById(Long jobId);
    //根据职位id增加投递数量
    @Update("update jobs set apply_count = apply_count + 1 where id = #{jobId}")
    int addApplyCountById(Long jobId);

    //面试通过时减少剩余岗位数
    @Update("update jobs set has_count = has_count - 1 where id = #{jobId} and has_count > 0")
    int decrementHasCount(Long jobId);

    //岗位招满时强制下架
    @Update("update jobs set status = 3 where id = #{jobId}")
    int forceOffline(Long jobId);

    List<Job> selectBatchIds(@Param("idList") Set<Long> idList);

    @Select("select * from jobs where company_id = #{id} and status = 1 order by updated_at limit #{start},#{total} ")
    List<Job> getJobsByCompanyId(Long id,Integer start,Integer total);

    /** 获取公司所有岗位，按投递数降序 */
    @Select("select * from jobs where company_id = #{companyId} and has_count > 0 order by apply_count desc")
    List<Job> getActiveJobsByCompanyId(Long companyId);

    @Select("select count(*) from jobs where company_id = #{id}")
    Integer countByCompanyId(Integer id);

    /** 统计在招职位数（status=1） */
    @Select("SELECT COUNT(*) FROM jobs WHERE company_id = #{companyId} AND status = 1")
    int countActiveByCompanyId(Long companyId);


    List<Job> getjobsBySkill(@Param("userSkill") String[] userSkill);

    @Select("select * from jobs order by view_count desc limit 5 ")
    List<Job> getJobRandom();

    @Select("select * from jobs where status = 1 order by apply_count desc, view_count desc limit #{limit}")
    List<Job> getHotJobs(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM jobs WHERE status = 1")
    Long countActiveJobs();

    Page<Job> getALLJobBuCompanyId(@Param("jobQueryDTO") JobQueryDTO jobQueryDTO,
                                   @Param("companyId") Long companyId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    void insert(JobDTO job);

    void updata(JobDTO job);

    @Select("select * from jobs where id = #{id}")
    Job getJobBy(Long id);

    @Select("SELECT COUNT(*) FROM jobs WHERE status = 1")
    Long countAllActive();

    /** 管理员职位列表：分页 + 筛选 + 企业名 */
    List<AdminJobVO> selectAdminList(@Param("title") String title,
                                     @Param("companyName") String companyName,
                                     @Param("status") Integer status);

    /** 管理员编辑职位 */
    @Update("UPDATE jobs SET title = #{title}, category = #{category}, city = #{city}, " +
            "salary_min = #{salaryMin}, salary_max = #{salaryMax}, education = #{education}, " +
            "experience = #{experience}, headcount = #{headcount}, updated_at = NOW() WHERE id = #{id}")
    int updateAdmin(Job job);

    @Delete("DELETE FROM jobs WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COALESCE(SUM(apply_count), 0) FROM jobs")
    Long sumApplyCount();

    @Select("SELECT COALESCE(SUM(apply_count), 0) FROM jobs WHERE company_id = #{companyId}")
    Long sumApplyCountByCompanyId(@Param("companyId") Long companyId);

    @Select("SELECT j.id AS jobId, j.title AS jobTitle, COALESCE(c.name, '未知公司') AS companyName, j.apply_count AS applyCount FROM jobs j LEFT JOIN companies c ON j.company_id = c.id ORDER BY j.apply_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopJobsByApplyCount(@Param("limit") int limit);
}
