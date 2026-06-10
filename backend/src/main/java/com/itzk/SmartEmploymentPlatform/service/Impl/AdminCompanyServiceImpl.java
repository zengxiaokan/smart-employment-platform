package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminCompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.AdminCompanyService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import com.itzk.SmartEmploymentPlatform.utils.Constant;

@Service
public class AdminCompanyServiceImpl implements AdminCompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageInfo<AdminCompanyVO> list(int page, int size, String name, String industry, Integer auditStatus) {
        PageHelper.startPage(page, size);
        List<AdminCompanyVO> list = companyMapper.selectAdminList(name, industry, auditStatus);
        return new PageInfo<>(list);
    }

    @Override
    public AdminCompanyVO detail(Long id) {
        Company c = companyMapper.getById(id);
        if (c == null) throw new BusinessException("企业不存在");
        AdminCompanyVO vo = new AdminCompanyVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setIndustry(c.getIndustry());
        vo.setSize(c.getSize());
        vo.setCity(c.getCity());
        vo.setAddress(c.getAddress());
        vo.setAuditStatus(c.getAuditStatus() != null ? c.getAuditStatus().intValue() : null);
        vo.setPhone(c.getPhone());
        vo.setFinancingStage(c.getFinancingStage() != null ? c.getFinancingStage().intValue() : null);
        vo.setOfficialWeb(c.getOfficialWeb());
        vo.setDescription(c.getDescription());
        vo.setLogoUrl(c.getLogoUrl());
        vo.setLicenseUrl(c.getLicenseUrl());
        vo.setAuditRemark(c.getAuditRemark());
        vo.setJobCount(c.getJobCount());
        vo.setJobConfirm(c.getJobConfirm());
        vo.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString().replace("T", " ") : null);

        if (c.getUserId() != null) {
            User u = userMapper.getUserById(c.getUserId());
            if (u != null) {
                vo.setRegistrant(u.getRealname() != null ? u.getRealname() : u.getNickname());
            }
        }
        return vo;
    }

    @Override
    public AdminUserVO getRegistrant(Long companyId) {
        Company c = companyMapper.getById(companyId);
        if (c == null) throw new BusinessException("企业不存在");
        if (c.getUserId() == null) throw new BusinessException("该企业未关联注册人");
        User u = userMapper.getUserById(c.getUserId());
        if (u == null) throw new BusinessException("注册人不存在");
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setPhone(u.getPhone());
        vo.setAvatarUrl(u.getAvatarUrl());
        vo.setRealname(u.getRealname());
        vo.setNickname(u.getNickname());
        vo.setCity(u.getCity());
        vo.setRole(u.getRole() != null ? u.getRole().intValue() : null);
        vo.setStatus(u.getStatus() != null ? u.getStatus().intValue() : null);
        vo.setGender(u.getGender() != null ? u.getGender().intValue() : null);
        vo.setAge(u.getAge());
        vo.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString().replace("T", " ") : null);
        return vo;
    }

    @Override
    @Transactional
    public void create(Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) throw new BusinessException("企业名称不能为空");

        Company c = new Company();
        c.setName(name);
        c.setPhone((String) body.get("phone"));
        c.setIndustry((String) body.get("industry"));
        c.setSize((String) body.get("size"));
        c.setCity((String) body.get("city"));
        c.setAddress((String) body.get("address"));
        c.setDescription((String) body.get("description"));
        c.setOfficialWeb((String) body.get("officialWeb"));
        if (body.get("financingStage") != null)
            c.setFinancingStage(((Number) body.get("financingStage")).byteValue());
        c.setAuditStatus((byte) 0);
        c.setUserId(UserHolder.getUserId());
        c.setCreatedAt(java.time.LocalDateTime.now());

        companyMapper.insert(c);
    }

    @Override
    @Transactional
    public void update(Long id, Map<String, Object> body) {
        Company c = new Company();
        c.setId(id);
        c.setName((String) body.get("name"));
        c.setPhone((String) body.get("phone"));
        c.setIndustry((String) body.get("industry"));
        c.setSize((String) body.get("size"));
        c.setCity((String) body.get("city"));
        c.setAddress((String) body.get("address"));
        c.setDescription((String) body.get("description"));
        c.setOfficialWeb((String) body.get("officialWeb"));
        if (body.get("financingStage") != null)
            c.setFinancingStage(((Number) body.get("financingStage")).byteValue());

        companyMapper.updateAdmin(c);
    }

    @OperationLog(action = "AUDIT_COMPANY", targetType = "company", targetId = "#id",
                  remark = "'auditStatus=' + #auditStatus + (#remark != null ? ', remark=' + #remark : '')")
    @Override
    @Transactional
    public void audit(Long id, Integer auditStatus, String remark) {
        if (auditStatus == null
                || auditStatus < 0 || auditStatus > 2) {
            throw new BusinessException("审核状态非法");
        }
        if (auditStatus == Constant.AuditStatus.REJECTED
                && (remark == null || remark.trim().isEmpty())) {
            throw new BusinessException("拒绝时必须填写拒绝理由");
        }
        if (remark != null && remark.length() > 500) {
            throw new BusinessException("审核备注不能超过500字");
        }
        Company c = new Company();
        c.setId(id);
        c.setAuditStatus(auditStatus.byteValue());
        c.setAuditRemark(remark);
        companyMapper.updataById(c);
    }

    @OperationLog(action = "DELETE_COMPANY", targetType = "company", targetId = "#id")
    @Override
    @Transactional
    public void delete(Long id) {
        companyMapper.deleteById(id);
    }
}
