package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyFromDTO {

    /** 企业名称 */
    @NotBlank(message = "企业名称不能为空")
    @Size(max = 50, message = "企业名称不能超过50字")
    private String companyName;

    /** 所属行业 */
    @NotBlank(message = "所属行业不能为空")
    @Size(max = 30, message = "所属行业不能超过30字")
    private String industry;

    /** 企业规模：0-0-20人，1-20-99人，2-100-499人，3-500-999人，4-1000人以上 */
    @NotBlank(message = "企业规模不能为空")
    @Pattern(regexp = "^[0-4]$", message = "企业规模取值非法")
    private String size;

    /** 所在城市 */
    @NotBlank(message = "所在城市不能为空")
    @Size(max = 30, message = "城市名称不能超过30字")
    private String city;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 100, message = "详细地址不能超过100字")
    private String address;

    /** 企业Logo URL */
    @NotBlank(message = "请上传企业Logo")
    private String logoUrl;

    /** 营业执照URL */
    @NotBlank(message = "请上传营业执照")
    private String licenseUrl;

    /** 企业官网（可选，但填了就要像样） */
    @Pattern(regexp = "^(https?://.+)?$", message = "官网地址必须以 http:// 或 https:// 开头")
    private String officialWeb;

    /** 企业联系电话 */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的11位手机号")
    private String phone;

    /** 融资阶段 */
    @NotNull(message = "请选择融资阶段")
    private Byte financingStage;

    /** 申请人用户ID（由 Controller 注入，不参与校验） */
    private Long userId;

    /** 申请状态（由 Service 强制置为待审核，不接收前端传入） */
    private Byte auditStatus;

}
