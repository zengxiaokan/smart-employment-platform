package com.itzk.SmartEmploymentPlatform.controller.recruitment;


import com.itzk.SmartEmploymentPlatform.pojo.Result;

import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyFromDTO;
import com.itzk.SmartEmploymentPlatform.service.CompanyApplyService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/hr/company")
public class CompanyApplyController {

    @Autowired
    private CompanyApplyService companyApplyService;

    @PostMapping("/apply")
    public Result commitCompany(@RequestBody CompanyFromDTO companyFromDTO){
        Long userId = UserHolder.getUserId();
        companyFromDTO.setUserId(userId);
        return companyApplyService.insertApply(companyFromDTO);

    }
}
