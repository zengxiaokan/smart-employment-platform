package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyJobsVo {

    private CompanyVO companyVO;

    private List<SimpleJobDetailVo> jobList;

}
