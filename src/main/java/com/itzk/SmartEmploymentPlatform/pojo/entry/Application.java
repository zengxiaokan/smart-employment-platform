package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位申请实体类
 * 对应数据库中的职位申请表，记录用户对职位的投递信息
 */
@Data
public class Application {

    /** 申请记录ID */
    private Long id;

    /** 职位ID */
    private Long jobId;

    /** 简历ID */
    private Long resumeId;

    /** 申请人用户ID */
    private Long userId;

    /** 所属公司ID */
    private Long companyId;

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

    /** 更新时间 */
    private LocalDateTime updatedAt;


    //操作人id
    private Long operatorId;
}
