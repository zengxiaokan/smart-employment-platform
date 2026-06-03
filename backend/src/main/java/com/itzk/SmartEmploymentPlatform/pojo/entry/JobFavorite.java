package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位收藏实体类
 * 对应数据库中的职位收藏表，记录用户收藏的职位信息
 */
@Data
public class JobFavorite {

    /** 收藏记录ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 职位ID */
    private Long jobId;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}
