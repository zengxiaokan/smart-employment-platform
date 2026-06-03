package com.itzk.SmartEmploymentPlatform.mapper;


import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;

import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminCompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyVO;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CompanyMapper {

    //根据职位中的企业id查询企业
    @Select("select * from companies where id = #{companyId}")
    Company getByJobId(@NotNull(message = "企业ID不能为空") Long companyId);

    //查询所有行业
    List<String> getDistinctIndustries();

    //查询规模
    List<String> getDistinctSizes();

    List<CompanyVO> selectCompanyList(CompanyQueryDTO queryDTO);

    @Select("Select * from companies where id = #{id}")
    Company getById(Long id);


    void insert(Company company);


    void updataById(Company company);

    /** 发布岗位时递增公司岗位数（按headcount增量） */
    @org.apache.ibatis.annotations.Update("UPDATE companies SET job_count = job_count + #{headcount} WHERE id = #{companyId}")
    int incrementJobCount(@org.apache.ibatis.annotations.Param("companyId") Long companyId,
                          @org.apache.ibatis.annotations.Param("headcount") int headcount);

    /** 下架或删除岗位时递减公司岗位数 */
    @org.apache.ibatis.annotations.Update("UPDATE companies SET job_count = GREATEST(job_count - #{headcount}, 0) WHERE id = #{companyId}")
    int decrementJobCount(@org.apache.ibatis.annotations.Param("companyId") Long companyId,
                          @org.apache.ibatis.annotations.Param("headcount") int headcount);

    /** 面试成功时递增公司已确认入职数 */
    @org.apache.ibatis.annotations.Update("UPDATE companies SET job_confirm = COALESCE(job_confirm, 0) + 1 WHERE id = #{companyId}")
    int incrementJobConfirm(Long companyId);

    @Select("SELECT COUNT(*) FROM companies WHERE audit_status = 1")
    Long countAudited();

    /** 管理员企业列表：分页 + 筛选 + 注册人 */
    List<AdminCompanyVO> selectAdminList(@Param("name") String name,
                                         @Param("industry") String industry,
                                         @Param("auditStatus") Integer auditStatus);

    /** 管理员编辑企业 */
    @Update("UPDATE companies SET name = #{name}, phone = #{phone}, industry = #{industry}, " +
            "size = #{size}, city = #{city}, financing_stage = #{financingStage}, " +
            "official_web = #{officialWeb}, address = #{address}, description = #{description}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateAdmin(Company company);

    @Delete("DELETE FROM companies WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
