package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminApplicationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.service.AdminApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminApplicationServiceImpl implements AdminApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Override
    public PageInfo<AdminApplicationVO> list(int page, int size, String applicantName, String jobTitle, Integer status) {
        PageHelper.startPage(page, size);
        List<AdminApplicationVO> list = applicationMapper.selectAdminList(applicantName, jobTitle, status);
        return new PageInfo<>(list);
    }

    @Override
    public ApplicationDitailVo detail(Long id) {
        ApplicationDitailVo vo = applicationMapper.getJobAndCompanyInfo(id);
        if (vo == null) throw new BusinessException("投递记录不存在");
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        applicationMapper.deleteById(id);
    }
}
