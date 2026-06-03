package com.itzk.SmartEmploymentPlatform.pojo.entry;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试邀约实体类
 * 对应数据库中的 interview_invitations 表
 */
@Data
public class Interview {

    /** 面试邀约ID */
    private Long id;

    /** 投递记录ID（关联applications表） */
    private Long applicationId;

    /** 公司ID（冗余，便于查询） */
    private Long companyId;

    /** 职位ID（冗余） */
    private Long jobId;

    /** 求职者用户ID（冗余） */
    private Long userId;

    /** 发送邀约的HR用户ID */
    private Long hrUserId;

    /** 面试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;

    /** 面试地点（可为线上链接或详细地址） */
    private String interviewLocation;

    /** 地点类型：0=线下 1=线上 */
    private Integer locationType;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** HR备注（如需要携带的材料等） */
    private String remark;

    /** 状态：0=待确认 1=接受 2=拒绝 3=已过期 4=已取消 */
    private Integer status;

    /** 求职者回复备注 */
    private String candidateRemark;

    /** 求职者回复时间 */
    private LocalDateTime responseTime;

    /** 邀约创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
