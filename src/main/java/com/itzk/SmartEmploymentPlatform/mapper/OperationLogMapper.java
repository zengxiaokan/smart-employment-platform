package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog;
import com.itzk.SmartEmploymentPlatform.pojo.vo.OperationLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    int insert(OperationLog log);

    OperationLogVO selectById(@Param("id") Long id);

    List<OperationLogVO> selectByCondition(@Param("userId") Long userId,
                                           @Param("action") String action,
                                           @Param("targetType") String targetType,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);

    int deleteOlderThan(@Param("days") int days);

    int updateRemark(@Param("id") Long id, @Param("remark") String remark);

    List<java.util.Map<String, Object>> countTodayByAction();
}
