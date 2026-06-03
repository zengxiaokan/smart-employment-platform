package com.itzk.SmartEmploymentPlatform.service;

import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Feedback;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;

public interface FeedbackService {
    void submit(Feedback feedback);
    PageInfo<Feedback> listByUser(Long userId, int page, int size);
    PageInfo<FeedbackVO> listAll(int page, int size, Integer type, Integer status);
    FeedbackVO detail(Long id);
    void reply(Long id, String reply);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
}
