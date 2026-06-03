package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公司待办事项
 */
@Data
public class CompanyTodo {

    /** 主键ID */
    private Long id;

    /** 公司ID */
    private Long companyId;

    /** 待办内容，最长50字 */
    private String content;

    /** 事件时间（面试时间） */
    private LocalDateTime eventTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
