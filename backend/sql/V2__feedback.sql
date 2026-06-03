-- =====================================================
-- 反馈工单表（用户提交 Bug / 建议 / 举报）
-- 公告复用 messages 表，msg_type=1
-- =====================================================
CREATE TABLE IF NOT EXISTS `feedbacks` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '工单ID',
    `user_id`     BIGINT        NOT NULL                 COMMENT '提交人用户ID',
    `type`        TINYINT       NOT NULL DEFAULT 0       COMMENT '类型：0=Bug反馈 1=功能建议 2=其他 3=举报',
    `title`       VARCHAR(100)  NOT NULL                 COMMENT '反馈标题',
    `content`     TEXT          NOT NULL                 COMMENT '反馈内容',
    `images`      VARCHAR(1000) DEFAULT NULL             COMMENT '附图URL（逗号分隔，最多3张）',
    `status`      TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0=待处理 1=处理中 2=已解决 3=已关闭',
    `reply`       TEXT          DEFAULT NULL             COMMENT '超管回复内容',
    `replied_at`  DATETIME      DEFAULT NULL             COMMENT '回复时间',
    `target_type` TINYINT       DEFAULT NULL             COMMENT '举报目标：NULL=无 1=职位 2=公司',
    `target_id`   BIGINT        DEFAULT NULL             COMMENT '被举报的职位ID或公司ID',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '提交时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反馈工单表';
