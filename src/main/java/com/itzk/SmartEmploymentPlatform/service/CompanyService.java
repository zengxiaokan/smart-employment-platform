package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryJobsByCompanyIdDTO;

import java.util.List;
import java.util.Map;

public interface CompanyService {


    Map<String, List<String>> getFilterItems();

    Result<PageResult> getCompanyList(CompanyQueryDTO queryDTO);

    //通过id查询公司及其职位
    Result getCompanyJobsDetail(Long id);

    //分页查询职位
    Result PageQueryJobsByCompanyId(Long companyId, QueryJobsByCompanyIdDTO query);

    Result getCompanyStatus(Long companyId);

    //更新公司信息
    Result updata(Company company);
}