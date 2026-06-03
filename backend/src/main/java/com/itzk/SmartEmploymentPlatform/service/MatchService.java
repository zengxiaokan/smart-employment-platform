package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchDetailVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchVO;

import java.util.List;

/**
 * AI匹配服务
 * 流程：岗位列表（按投递数排序）→ 点岗位 → 智能评分 → AI分析
 */
public interface MatchService {

    /** 获取公司所有在招岗位，按投递人数降序 */
    List<Job> listCompanyJobs(Long companyId);

    /** 查看某岗位的匹配结果列表（按分数降序），首次自动评分入库 */
    List<MatchVO> listByJobId(Long jobId);

    /** 查看匹配详情（含AI推荐语和技能逐项明细） */
    MatchDetailVO getDetail(Long matchId);

    /** 重新生成AI评语（用已有评分数据重新调AI），返回新的评语文本 */
    String regenerateSummary(Long matchId);
}
