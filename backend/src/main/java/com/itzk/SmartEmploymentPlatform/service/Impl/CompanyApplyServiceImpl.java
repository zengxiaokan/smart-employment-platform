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
import java.util.regex.Pattern;

@Service
public class CompanyApplyServiceImpl implements CompanyApplyService {

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern SIZE_CODE = Pattern.compile("^[0-4]$");
    private static final Pattern URL_PREFIX = Pattern.compile("^(https?://.+)?$");



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

        // 检查用户是否已绑定企业
        Long userId = companyFromDTO.getUserId();
        if (userId != null) {
            var existingUser = userMapper.getUserById(userId);
            if (existingUser != null && existingUser.getCompanyId() != null
                    && existingUser.getCompanyId() > 0) {
                return Result.error("您已绑定企业，无法重复申请");
            }
        }

        // 防御性二次校验（@Valid 已被 Controller 调用，但 Service 还要兜底，
        // 防止被 AOP / 其他入口绕过；同时也防止脏数据写入）
        String v = requireNonBlank(companyFromDTO.getCompanyName(), "企业名称");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getIndustry(), "所属行业");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getSize(), "企业规模");
        if (v != null) return Result.error(v);
        if (!SIZE_CODE.matcher(companyFromDTO.getSize()).matches()) {
            return Result.error("企业规模取值非法");
        }
        v = requireNonBlank(companyFromDTO.getCity(), "所在城市");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getAddress(), "详细地址");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getLogoUrl(), "企业Logo");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getLicenseUrl(), "营业执照");
        if (v != null) return Result.error(v);
        v = requireNonBlank(companyFromDTO.getPhone(), "联系电话");
        if (v != null) return Result.error(v);
        if (!PHONE.matcher(companyFromDTO.getPhone()).matches()) {
            return Result.error("请输入正确的11位手机号");
        }
        if (companyFromDTO.getFinancingStage() == null) {
            return Result.error("请选择融资阶段");
        }
        if (companyFromDTO.getOfficialWeb() != null && !companyFromDTO.getOfficialWeb().isEmpty()
                && !URL_PREFIX.matcher(companyFromDTO.getOfficialWeb()).matches()) {
            return Result.error("官网地址必须以 http:// 或 https:// 开头");
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
        if (userId == null) {
            return Result.error("用户异常");
        }
        userMapper.updateCompanyId(userId,companyId);
        //插入申请表等待审核



        return Result.success(company);
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + "不能为空";
        }
        return null;
    }
}
