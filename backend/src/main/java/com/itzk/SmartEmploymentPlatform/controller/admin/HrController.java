package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminHrVO;
import com.itzk.SmartEmploymentPlatform.service.AdminUserService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController("adminHrController")
@RequestMapping("/admin/hrs")
public class HrController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult> list(@Min(1) @RequestParam(defaultValue = "1") int page,
                                   @Min(1) @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) Integer auditStatus,
                                   @RequestParam(required = false) Integer status) {
        PageInfo<AdminHrVO> pageInfo = adminUserService.listHrs(page, size, name, auditStatus, status);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        adminUserService.createHr(body);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminUserService.updateHr(id, body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminUserService.deleteHr(id);
        return Result.success();
    }
}
