package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.mapper.OperationLogMapper;
import com.itzk.SmartEmploymentPlatform.pojo.vo.OperationLogVO;
import com.itzk.SmartEmploymentPlatform.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public PageInfo<OperationLogVO> query(int page, int size, Long userId, String action,
                                          String targetType, String startTime, String endTime) {
        PageHelper.startPage(page, size);
        List<OperationLogVO> list = operationLogMapper.selectByCondition(userId, action, targetType, startTime, endTime);
        return new PageInfo<>(list);
    }

    @Override
    public OperationLogVO getDetail(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public void updateRemark(Long id, String remark) {
        operationLogMapper.updateRemark(id, remark);
    }

    @Override
    public java.util.Map<String, Long> todayStats() {
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        long total = 0;
        for (java.util.Map<String, Object> row : operationLogMapper.countTodayByAction()) {
            long cnt = ((Number) row.get("cnt")).longValue();
            result.put((String) row.get("action"), cnt);
            total += cnt;
        }
        result.put("total", total);
        return result;
    }
}
