package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Feedback;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;
import com.itzk.SmartEmploymentPlatform.service.FeedbackService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public Result<PageResult> list(@Min(1) @RequestParam(defaultValue = "1") int page,
                                    @Min(1) @RequestParam(defaultValue = "10") int size) {
        Long userId = UserHolder.getUserId();
        PageInfo<Feedback> pageInfo = feedbackService.listByUser(userId, page, size);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @PostMapping
    public Result submit(@Valid @RequestBody Feedback feedback) {
        feedback.setUserId(UserHolder.getUserId());
        feedbackService.submit(feedback);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<FeedbackVO> detail(@PathVariable Long id) {
        FeedbackVO vo = feedbackService.detail(id);
        if (vo == null || !vo.getUserId().equals(UserHolder.getUserId())) {
            return Result.error("反馈不存在");
        }
        return Result.success(vo);
    }
}
