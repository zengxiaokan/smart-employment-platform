package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyFromDTO;

public interface CompanyApplyService {


    /**
     * 申请公司资质
     * @param companyFromDTO
     * @return
     */
    Result insertApply(CompanyFromDTO companyFromDTO);

}
