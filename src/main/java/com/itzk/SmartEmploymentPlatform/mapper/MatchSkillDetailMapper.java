package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.MatchSkillDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MatchSkillDetailMapper {

    /** 批量写入技能明细 */
    int batchInsert(List<MatchSkillDetail> list);

    /** 按匹配ID查技能明细 */
    List<MatchSkillDetail> selectByMatchId(Long matchId);

    /** 批量查技能明细（避免N+1，用于匹配列表渲染） */
    List<MatchSkillDetail> selectByMatchIds(List<Long> matchIds);
}
