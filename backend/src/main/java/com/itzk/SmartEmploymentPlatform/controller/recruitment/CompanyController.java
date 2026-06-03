package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.service.CompanyService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("recruitmentCompanyController")
@Slf4j
@RequestMapping("hr/company")
public class CompanyController {


    @Autowired
    private CompanyService companyService;

    /**
     * 获取注册信息
     * @return
     */
    @GetMapping("/application/status")
    public Result getCompanyStatus(){
        Long companyId = UserHolder.getCompanyId();
        return companyService.getCompanyStatus(companyId);
    }

    @PutMapping("info")
    public Result updataCompanyInfo(@RequestBody Company company){
        log.info("修改的数据为{}",company);
        return companyService.updata(company);
    }




}
