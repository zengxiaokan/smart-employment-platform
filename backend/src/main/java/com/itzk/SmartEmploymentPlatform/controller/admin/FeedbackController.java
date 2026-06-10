package com.itzk.SmartEmploymentPlatform.controller.admin;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;
import com.itzk.SmartEmploymentPlatform.service.FeedbackService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController("adminFeedbackController")
@RequestMapping("/admin/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public Result<PageResult> list(@Min(1) @RequestParam(defaultValue = "1") int page,
                                    @Min(1) @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Integer type,
                                    @RequestParam(required = false) Integer status) {
        PageInfo<FeedbackVO> pageInfo = feedbackService.listAll(page, size, type, status);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @GetMapping("/{id}")
    public Result<FeedbackVO> detail(@PathVariable Long id) {
        return Result.success(feedbackService.detail(id));
    }

    @PutMapping("/{id}/reply")
    public Result reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        feedbackService.reply(id, body.get("reply"));
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        feedbackService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return Result.success();
    }
}
