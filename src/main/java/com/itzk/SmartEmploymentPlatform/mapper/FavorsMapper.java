package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.JobFavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavorsMapper {


    @Insert("INSERT INTO job_favorites (user_id, job_id, created_at) " +
            "VALUES (#{userId}, #{jobId}, now())")
    int insert(JobFavor jobFavor);

    @Delete("delete from job_favorites where job_id = #{jobId} and user_id = #{userId}")
    int deleteByJobIdAndUserId(@Param("jobId") Long jobId, @Param("userId") Long userId);

    @Select("SELECT job_id FROM job_favorites WHERE user_id = #{userId}")
    List<Long> selectJobIdsByUserId(Long userId);
}
