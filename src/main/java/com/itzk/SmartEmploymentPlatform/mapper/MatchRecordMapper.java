package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.MatchRecord;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MatchVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MatchRecordMapper {

    int insert(MatchRecord record);

    /** 按岗位查匹配记录（仅match_records表） */
    List<MatchRecord> selectByJobId(Long jobId);

    /** 按岗位查匹配列表，LEFT JOIN resumes 获取姓名+头像 */
    List<MatchVO> selectVOByJobId(Long jobId);

    MatchRecord selectById(Long id);

    /** 更新AI推荐语 */
    int updateSummary(Long id, String summary);
}
