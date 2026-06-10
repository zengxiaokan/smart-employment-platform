-- =====================================================
-- 企业审核拒绝理由
-- 管理员拒绝时填写，HR 在 SetupPending 页面可见
-- =====================================================
ALTER TABLE `companies`
    ADD COLUMN `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注/拒绝理由' AFTER `audit_status`;
