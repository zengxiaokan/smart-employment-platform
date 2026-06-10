package com.itzk.SmartEmploymentPlatform.utils;

// RedisKeyConstants.java
public class RedisKeyConstants {
    // 用户收藏集合：key=collect:用户ID，value=Set<职位ID>
    public static final String USER_COLLECT_PREFIX = "collect:user:";
    
    // 职位浏览量：key=view:count:职位ID，value=浏览量增量
    public static final String USER_DAILY_VIEW_PREFIX = "view:jobs:user:";
    
    // 用户浏览记录：key=view:history:用户ID，value=ZSet<职位ID, 时间戳>
    public static final String USER_VIEW_HISTORY_PREFIX = "view:history:";

    //用户岗位投递
    public static final String JOB_VIEW_SUBMITTED_PREFIX = "view:submitted:user:";


    // 投递冷却锁：key = apply:lock:用户ID:职位ID
    public static final String APPLY_LOCK_PREFIX = "apply:lock:";

    // 公司重新申请冷却锁：key = company:reapply:lock:公司ID
    // 防止 HR 短时间内反复 2→0→2→0 刷管理员,锁 24h
    public static final String COMPANY_REAPPLY_LOCK_PREFIX = "company:reapply:lock:";

    //收藏冷却锁
    public static final String FavorJob_LOCK_PREFIX = "favorjob:lock:";

    // 数据分析概览缓存：key = analytics:overview:公司ID:开始日期:结束日期
    public static final String ANALYTICS_OVERVIEW_PREFIX = "analytics:overview:";

    //hr收藏用户简历
    public static final String FAVORRESUME_PREFIX = "collect:hr:";

    //hr收藏用户数据缓存
    public static final String COLLECT_RESUME_PREFIX = "collect:hr:resume:";

    // WebSocket 在线状态
    public static final String ONLINE_USER_PREFIX = "online:user:";

    // 职位列表共享缓存（非个性化数据）
    public static final String JOBS_LIST_CACHE_PREFIX = "jobs:list:";

    // 热门岗位缓存
    public static final String JOBS_HOT_CACHE_PREFIX = "jobs:hot:";

    // 缓存过期时间
    public static final long JOBS_CACHE_TTL_MINUTES = 15;

    // 收藏集合过期天数（到期后从 MySQL 重建，防止数据不一致长期存在）
    public static final long FAVOR_SET_TTL_DAYS = 7;
}