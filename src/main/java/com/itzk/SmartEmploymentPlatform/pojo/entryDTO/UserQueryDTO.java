package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 用户列表查询请求参数DTO
 * 用于分页查询用户列表，支持关键词、角色、状态等条件筛选
 */
@Data
public class UserQueryDTO {

    /**
     * 当前页码，默认第1页
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    /**
     * 每页条数，默认10条
     */
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer size = 10;

    /** 搜索关键词（昵称、手机号、邮箱等模糊搜索） */
    private String keyword;

    /** 用户角色：0-求职者，1-HR，2-管理员 */
    private Byte role;

    /** 账号状态：0-禁用，1-正常 */
    private Byte status;
}
