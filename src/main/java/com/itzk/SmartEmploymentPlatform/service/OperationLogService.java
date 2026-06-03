package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.OperationLogVO;

public interface OperationLogService {

    PageInfo<OperationLogVO> query(int page, int size, Long userId, String action,
                                   String targetType, String startTime, String endTime);

    OperationLogVO getDetail(Long id);

    void updateRemark(Long id, String remark);

    java.util.Map<String, Long> todayStats();
}
