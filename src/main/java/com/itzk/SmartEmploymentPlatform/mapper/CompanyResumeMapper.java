package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.FavorResumDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryResumeDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CompanyResumeMapper {


    void insert(FavorResumDTO dto);


    @Delete("delete from company_resume where resume_id = #{resumeId} and company_id = #{companyId}")
    void deleteBycompanyIdAndResumeId(Long companyId, Long resumeId);

    List<FavorResumDTO> getRemarkByIds(List<Long> ids);

    @Update("update company_resume set remark = #{reason} where resume_id = #{resumeId} and company_id = #{companyId} ")
    void updataRemark(Long resumeId, String reason,Long companyId);

    @Select("SELECT resume_id FROM company_resume WHERE company_id = #{companyId}")
    List<Long> selectResumeIdsByCompanyId(Long companyId);
}
