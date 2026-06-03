package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.CompanyTodo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公司待办事项 Mapper
 */
@Mapper
public interface CompanyTodoMapper {

    /** 插入一条待办记录 */
    void insert(CompanyTodo todo);

    /** 查询公司最近5条待办 */
    @Select("SELECT * FROM company_todos WHERE company_id = #{companyId} ORDER BY created_at DESC LIMIT 5")
    List<CompanyTodo> getLatestByCompanyId(Long companyId);
}
