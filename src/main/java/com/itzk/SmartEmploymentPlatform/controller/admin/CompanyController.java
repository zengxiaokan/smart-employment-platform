package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminCompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;
import com.itzk.SmartEmploymentPlatform.service.AdminCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminCompanyController")
@RequestMapping("/admin/companies")
public class CompanyController {

    @Autowired
    private AdminCompanyService adminCompanyService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String industry,
                                   @RequestParam(required = false) Integer auditStatus) {
        PageInfo<AdminCompanyVO> pageInfo = adminCompanyService.list(page, size, name, industry, auditStatus);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @GetMapping("/{id}")
    public Result<AdminCompanyVO> detail(@PathVariable Long id) {
        return Result.success(adminCompanyService.detail(id));
    }

    @GetMapping("/{id}/registrant")
    public Result<AdminUserVO> getRegistrant(@PathVariable Long id) {
        return Result.success(adminCompanyService.getRegistrant(id));
    }

    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        adminCompanyService.create(body);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminCompanyService.update(id, body);
        return Result.success();
    }

    @PutMapping("/{id}/audit")
    public Result audit(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminCompanyService.audit(id, body.get("auditStatus"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminCompanyService.delete(id);
        return Result.success();
    }
}
