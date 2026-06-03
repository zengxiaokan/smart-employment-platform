package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyFromDTO;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.CompanyApplyService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CompanyApplyServiceImpl implements CompanyApplyService {



    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @OperationLog(action = "COMPANY_REGISTER", targetType = "company", targetId = "#result.data.id",
                  targetName = "#companyFromDTO.companyName")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result insertApply(CompanyFromDTO companyFromDTO) {


        if (companyFromDTO == null) {
            return Result.error("参数为空");
        }
        LocalDateTime  now = LocalDateTime.now();
        //创建公司实体
        Company company = new Company();
        BeanUtils.copyProperties(companyFromDTO, company);
        company.setName(companyFromDTO.getCompanyName());
        company.setAuditStatus(Constant.AuditStatus.PENDING);
        company.setJobCount(0);
        company.setJobConfirm(0);
        company.setCreatedAt(now);
        company.setUpdatedAt(now);
        //插入公司表中
        companyMapper.insert(company);

        Long companyId = company.getId();
        if (companyId == null) {
            return Result.error("插入失败");
        }
        Long userId = companyFromDTO.getUserId();
        if (userId == null) {
            return Result.error("用户异常");
        }
        userMapper.updateCompanyId(userId,companyId);
        //插入申请表等待审核



        return Result.success(company);
    }
}
