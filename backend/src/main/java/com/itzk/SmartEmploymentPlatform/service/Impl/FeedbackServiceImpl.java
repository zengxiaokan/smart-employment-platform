package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.FeedbackMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Feedback;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;
import com.itzk.SmartEmploymentPlatform.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    @Transactional
    public void submit(Feedback feedback) {
        if (feedback.getStatus() == null) feedback.setStatus(0);
        feedbackMapper.insert(feedback);
    }

    @Override
    public PageInfo<Feedback> listByUser(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Feedback> list = feedbackMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<FeedbackVO> listAll(int page, int size, Integer type, Integer status) {
        PageHelper.startPage(page, size);
        List<FeedbackVO> list = feedbackMapper.selectAdminList(type, status);
        return new PageInfo<>(list);
    }

    @Override
    public FeedbackVO detail(Long id) {
        Feedback f = feedbackMapper.selectById(id);
        if (f == null) throw new BusinessException("反馈不存在");
        FeedbackVO vo = new FeedbackVO();
        vo.setId(f.getId());
        vo.setUserId(f.getUserId());
        vo.setType(f.getType());
        vo.setTitle(f.getTitle());
        vo.setContent(f.getContent());
        vo.setImages(f.getImages());
        vo.setStatus(f.getStatus());
        vo.setReply(f.getReply());
        vo.setRepliedAt(f.getRepliedAt());
        vo.setCreatedAt(f.getCreatedAt());
        vo.setUpdatedAt(f.getUpdatedAt());
        vo.setTargetType(f.getTargetType());
        vo.setTargetId(f.getTargetId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long id, String reply) {
        if (UserHolder.getRole() == null || UserHolder.getRole() < Constant.Role.ADMIN) {
            throw new BusinessException("无权限操作");
        }
        feedbackMapper.updateReply(id, reply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (UserHolder.getRole() == null || UserHolder.getRole() < Constant.Role.ADMIN) {
            throw new BusinessException("无权限操作");
        }
        feedbackMapper.updateStatus(id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (UserHolder.getRole() == null || UserHolder.getRole() < Constant.Role.ADMIN) {
            throw new BusinessException("无权限操作");
        }
        feedbackMapper.deleteById(id);
    }
}
