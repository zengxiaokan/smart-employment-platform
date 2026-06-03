package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimpleApplicationVo {

    /** 申请记录ID */
    private Long id;

    /** 投递时间 */
    private LocalDateTime appliedAt;

    /** 查看时间 */
    private LocalDateTime viewedAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 职位名称 */
    private String jobTitle;

    /** 公司名称 */
    private String companyName;

    /** 申请人姓名 */
    private Long userId;

    /** 简历名称 */
    private String resumeName;
    /** 申请状态：0-待处理，1-已查看，2-邀请面试，3-不合适 */
    private Byte status;

    /** 面试时间 */
    private LocalDateTime interviewTime;

}
