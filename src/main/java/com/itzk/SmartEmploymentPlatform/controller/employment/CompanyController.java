package com.itzk.SmartEmploymentPlatform.controller.employment;


import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryJobsByCompanyIdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyVO;
import com.itzk.SmartEmploymentPlatform.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("employmentCompanyController")
@Slf4j
@RequestMapping("user/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    /**
     * 获取公司筛选条件：行业 + 规模
     */
    @GetMapping("/filters")
    public Result<Map<String, List<String>>> getCompanyFilterItems() {
        Map<String, List<String>> map = companyService.getFilterItems();
        return Result.success(map);
    }

    /**
     * 分页查询企业列表（支持关键词、行业、规模筛选）
     */
    @GetMapping("/list")
    public Result<PageResult> getCompanyList(CompanyQueryDTO queryDTO) {
        log.info("参数{}", queryDTO);
        return companyService.getCompanyList(queryDTO);
    }

    /**
     * 获取公司信息和发布岗位列表
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result getCompanyJobsDetail(@PathVariable("id") Long id) {
        log.info("公司id:{}",id);
        return companyService.getCompanyJobsDetail(id);
    }


    @GetMapping("/{companyId}/jobs")
    public Result getCompanyJobs(@PathVariable("companyId") Long companyId
            ,QueryJobsByCompanyIdDTO query) {
        log.info("查询第{}页",query.getPage());
        return companyService.PageQueryJobsByCompanyId(companyId,query);
    }

}
