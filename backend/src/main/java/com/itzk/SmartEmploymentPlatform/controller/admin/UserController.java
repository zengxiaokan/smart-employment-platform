package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;
import com.itzk.SmartEmploymentPlatform.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminUserController")
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer role,
                                   @RequestParam(required = false) Integer status) {
        PageInfo<AdminUserVO> pageInfo = adminUserService.listUsers(page, size, username, keyword, role, status);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        adminUserService.createUser(body);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminUserService.updateUser(id, body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success();
    }
}
