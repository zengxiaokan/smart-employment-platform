package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminResumeVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ResumeVO;
import com.itzk.SmartEmploymentPlatform.service.AdminResumeService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController("adminResumeController")
@RequestMapping("/admin/resumes")
public class ResumeController {

    @Autowired
    private AdminResumeService adminResumeService;

    @GetMapping
    public Result<PageResult> list(@Min(1) @RequestParam(defaultValue = "1") int page,
                                   @Min(1) @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String jobIntention) {
        PageInfo<AdminResumeVO> pageInfo = adminResumeService.list(page, size, name, jobIntention);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> detail(@PathVariable Long id) {
        return Result.success(adminResumeService.detail(id));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminResumeService.delete(id);
        return Result.success();
    }
}
