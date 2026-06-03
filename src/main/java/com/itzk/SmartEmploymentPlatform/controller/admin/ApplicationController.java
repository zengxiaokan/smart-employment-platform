package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminApplicationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.service.AdminApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminApplicationController")
@RequestMapping("/admin/applications")
public class ApplicationController {

    @Autowired
    private AdminApplicationService adminApplicationService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String applicantName,
                                   @RequestParam(required = false) String jobTitle,
                                   @RequestParam(required = false) Integer status) {
        PageInfo<AdminApplicationVO> pageInfo = adminApplicationService.list(page, size, applicantName, jobTitle, status);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @GetMapping("/{id}")
    public Result<ApplicationDitailVo> detail(@PathVariable Long id) {
        return Result.success(adminApplicationService.detail(id));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminApplicationService.delete(id);
        return Result.success();
    }
}
