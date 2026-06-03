package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.OperationLogVO;
import com.itzk.SmartEmploymentPlatform.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminLogController")
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String action,
                                   @RequestParam(required = false) String targetType,
                                   @RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime) {
        PageInfo<OperationLogVO> pageInfo = operationLogService.query(page, size, userId, action, targetType, startTime, endTime);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @GetMapping("/{id}")
    public Result<OperationLogVO> detail(@PathVariable Long id) {
        return Result.success(operationLogService.getDetail(id));
    }

    @PutMapping("/{id}/remark")
    public Result updateRemark(@PathVariable Long id, @RequestBody Map<String, String> body) {
        operationLogService.updateRemark(id, body.get("remark"));
        return Result.success();
    }

    @GetMapping("/stats/today")
    public Result<Map<String, Long>> todayStats() {
        return Result.success(operationLogService.todayStats());
    }
}
