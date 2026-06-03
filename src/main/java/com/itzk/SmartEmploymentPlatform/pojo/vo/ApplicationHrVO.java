package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位申请视图对象
 * 用于返回职位申请详情，包含申请信息及关联的职位、公司、申请人、简历名称
 */
@Data
public class ApplicationHrVO {

    /** 申请记录ID */
    private Long id;

    /** 职位ID */
    private Long jobId;

    /** 简历ID */
    private Long resumeId;
    /** 申请人用户ID */
    private Long userId;
    /** 申请状态：0-待处理，1-已查看，2-邀请面试，3-不合适 */
    private Byte status;
    /** 面试时间 */
    private LocalDateTime interviewTime;
    /** HR备注 */
    private String hrRemark;
    /** 投递时间 */
    private LocalDateTime appliedAt;
    /** 查看时间 */
    private LocalDateTime viewedAt;
    /** 职位名称 */
    private String jobTitle;
    /** 申请人姓名 */
    private String applicantName;



}
