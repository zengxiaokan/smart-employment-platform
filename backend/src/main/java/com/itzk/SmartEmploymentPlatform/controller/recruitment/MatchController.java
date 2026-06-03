package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchDetailVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchVO;
import com.itzk.SmartEmploymentPlatform.service.MatchService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HR端AI匹配接口
 * 流程：岗位列表（按投递数降序）→ 点岗位 → 智能评分 → AI分析
 */
@RestController
@RequestMapping("/hr/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    /** 公司所有在招岗位，按投递人数降序 */
    @GetMapping("/jobs")
    public Result<List<Job>> listCompanyJobs() {
        Long companyId = UserHolder.getCompanyId();
        List<Job> jobs = matchService.listCompanyJobs(companyId);
        return Result.success(jobs);
    }

    /** 某岗位的匹配列表（按总分降序），首次自动评分入库 */
    @GetMapping("/list/{jobId}")
    public Result<List<MatchVO>> listByJobId(@PathVariable Long jobId) {
        List<MatchVO> list = matchService.listByJobId(jobId);
        return Result.success(list);
    }

    /** 匹配详情，含AI推荐语和技能逐项明细 */
    @GetMapping("/detail/{matchId}")
    public Result<MatchDetailVO> getDetail(@PathVariable Long matchId) {
        MatchDetailVO vo = matchService.getDetail(matchId);
        return Result.success(vo);
    }

    /** 重新生成AI评语（已有评分不变，仅重新调AI生成评语） */
    @PostMapping("/refresh/{matchId}")
    public Result<String> refreshSummary(@PathVariable Long matchId) {
        String summary = matchService.regenerateSummary(matchId);
        return Result.success(summary);
    }
}
