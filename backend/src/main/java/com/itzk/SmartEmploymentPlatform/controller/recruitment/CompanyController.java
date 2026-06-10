package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.service.CompanyService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
    public Result updataCompanyInfo(@Valid @RequestBody Company company){
        log.info("修改的数据为{}",company);
        Long companyId = UserHolder.getCompanyId();
        if (company.getId() != null && !company.getId().equals(companyId)) {
            return Result.error("无权修改该公司信息");
        }
        company.setId(companyId);
        return companyService.updata(company);
    }

    /**
     * 重新申请：把被拒(auditStatus=2)状态重置为待审(0)
     * 加 Redis 24h 锁防刷
     */
    @PostMapping("/reapply")
    public Result reapply(){
        Long companyId = UserHolder.getCompanyId();
        return companyService.reapply(companyId);
    }




}
