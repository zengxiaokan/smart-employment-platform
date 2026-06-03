package com.itzk.SmartEmploymentPlatform.controller.employment;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Feedback;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;
import com.itzk.SmartEmploymentPlatform.service.FeedbackService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Long userId = UserHolder.getUserId();
        PageInfo<Feedback> pageInfo = feedbackService.listByUser(userId, page, size);
        return Result.success(new PageResult(pageInfo.getTotal(), pageInfo.getList()));
    }

    @PostMapping
    public Result submit(@RequestBody Feedback feedback) {
        feedback.setUserId(UserHolder.getUserId());
        feedbackService.submit(feedback);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<FeedbackVO> detail(@PathVariable Long id) {
        return Result.success(feedbackService.detail(id));
    }
}
