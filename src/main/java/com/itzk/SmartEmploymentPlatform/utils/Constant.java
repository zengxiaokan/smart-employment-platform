package com.itzk.SmartEmploymentPlatform.utils;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * 全局常量
 */
public final class Constant {

    private Constant() {}



    /** 投递状态 */
    public static final class ApplicationStatus {
        /** 待处理 / 已投递 */
        public static final byte PENDING = 0;
        /** HR 已查看 */
        public static final byte VIEWED = 1;
        /** 邀请面试 */
        public static final byte INTERVIEW = 2;
        /** 不合适 */
        public static final byte REJECTED = 3;
        //已拒绝
        public static final byte APPLY_CANCER = 4;

        //已录用
        public static final byte Hired = 5;

        //多次申请
        public static final byte APPLY_Double = 6;

        private ApplicationStatus() {}
    }

    /** 性别 */
    public static final class Gender {
        /** 女 */
        public static final byte FEMALE = 0;
        /** 男 */
        public static final byte MALE = 1;

        public static String label(byte code) {
            return code == MALE ? "男" : code == FEMALE ? "女" : "未知";
        }

        private Gender() {}
    }

    /** 学历（简历表 resume_educations 使用，值域 0-6） */
    public static final class Education {
        public static final byte JUNIOR_HIGH = 0;
        public static final byte HIGH_SCHOOL = 1;
        public static final byte TECHNICAL = 2;
        public static final byte ASSOCIATE = 3;
        public static final byte BACHELOR = 4;
        public static final byte MASTER = 5;
        public static final byte DOCTOR = 6;

        public static String label(byte code) {
            switch (code) {
                case JUNIOR_HIGH: return "初中";
                case HIGH_SCHOOL: return "高中";
                case TECHNICAL:   return "中专";
                case ASSOCIATE:   return "大专";
                case BACHELOR:    return "本科";
                case MASTER:      return "硕士";
                case DOCTOR:      return "博士";
                default:          return "不限";
            }
        }

        private Education() {}
    }

    /** 职位上下架状态 */
    public static final class JobStatus {
        /** 下架 */
        public static final byte REVIEWING = 0;
        /** 招聘中 */
        public static final byte ACTIVE = 1;
        /** 审核中 */
        public static final byte OFFLINE= 2;
        /** 强制下架 */
        public static final byte FORCE_OFFLINE = 3;

        public static String label(byte code) {
            switch (code) {
                case OFFLINE:       return "已下架";
                case ACTIVE:        return "招聘中";
                case REVIEWING:     return "审核中";
                case FORCE_OFFLINE: return "强制下架";
                default:            return "未知";
            }
        }

        private JobStatus() {}
    }

    /** 用户角色 */
    public static final class Role {
        /** 求职者 */
        public static final byte JOB_SEEKER = 0;
        /** HR */
        public static final byte HR = 1;
        /** 超级管理员 */
        public static final byte ADMIN = 2;

        public static String label(byte code) {
            switch (code) {
                case JOB_SEEKER: return "求职者";
                case HR:         return "HR";
                case ADMIN:      return "管理员";
                default:         return "未知";
            }
        }

        private Role() {}
    }

    /** 用户账号状态 */
    public static final class UserStatus {
        /** 正常 */
        public static final byte NORMAL = 0;
        /** 封号 */
        public static final byte BANNED = 1;

        private UserStatus() {}
    }

    /** 企业审核状态 */
    public static final class AuditStatus {
        /** 待审核 */
        public static final byte PENDING = 0;
        /** 已通过 */
        public static final byte APPROVED = 1;
        /** 未通过 */
        public static final byte REJECTED = 2;

        private AuditStatus() {}
    }

    /** 面试邀约状态 */
    public static final class InterviewStatus {
        /** 待确认 */
        public static final int PENDING = 0;
        /** 接受 */
        public static final int ACCEPTED = 1;
        /** 待定 */
        public static final int TENTATIVE = 2;
        /** 面试成功 */
        public static final int PASSED = 3;
        /** 拒绝 */
        public static final int REJECTED = 4;
        /** 已过期 */
        public static final int EXPIRED = 5;
        /** 已取消 */
        public static final int CANCELLED = 6;

        public static String label(int code) {
            switch (code) {
                case PENDING:   return "待确认";
                case ACCEPTED:  return "接受";
                case TENTATIVE: return "待定";
                case PASSED:    return "面试成功";
                case REJECTED:  return "拒绝";
                case EXPIRED:   return "已过期";
                case CANCELLED: return "已取消";
                default:        return "未知";
            }
        }

        private InterviewStatus() {}
    }

    /** 面试地点类型 */
    public static final class LocationType {
        /** 线下 */
        public static final int OFFLINE = 0;
        /** 线上 */
        public static final int ONLINE = 1;

        public static String label(int code) {
            return code == ONLINE ? "线上" : code == OFFLINE ? "线下" : "未知";
        }

        private LocationType() {}
    }

    /** 求职类型 */
    public static final class JobType {
        /** 不限 */
        public static final int ANY = 0;
        /** 全职 */
        public static final int FULL_TIME = 1;
        /** 兼职 */
        public static final int PART_TIME = 2;
        /** 实习 */
        public static final int INTERNSHIP = 3;

        public static String label(int code) {
            switch (code) {
                case FULL_TIME:  return "全职";
                case PART_TIME:  return "兼职";
                case INTERNSHIP: return "实习";
                default:         return "不限";
            }
        }

        /** 根据中文标签获取求职类型码 */
        public static int fromLabel(String label) {
            if (label == null) return ANY;
            switch (label.trim()) {
                case "全职": return FULL_TIME;
                case "兼职": return PART_TIME;
                case "实习": return INTERNSHIP;
                default:    return ANY;
            }
        }

        private JobType() {}
    }
}
