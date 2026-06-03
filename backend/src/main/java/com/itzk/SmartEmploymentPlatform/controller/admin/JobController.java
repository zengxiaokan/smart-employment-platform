package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminJobVO;
import com.itzk.SmartEmploymentPlatform.service.AdminJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminJobController")
@RequestMapping("/admin/jobs")
public class JobController {

    @Autowired
    private AdminJobService adminJobService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String title,
                                   @RequestParam(required = false) String companyName,
                                   @RequestParam(required = false) Integer status) {
        PageInfo<AdminJobVO> pageInfo = adminJobService.list(page, size, title, companyName, status);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        adminJobService.create(body);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminJobService.update(id, body);
        return Result.success();
    }

    @PutMapping("/{id}/audit")
    public Result audit(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminJobService.audit(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminJobService.toggleStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminJobService.delete(id);
        return Result.success();
    }
}
