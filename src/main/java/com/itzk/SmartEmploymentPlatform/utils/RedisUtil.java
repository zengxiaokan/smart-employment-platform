package com.itzk.SmartEmploymentPlatform.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 可用性熔断器 — Redis 不可用时快速跳过，避免每次调用都等待超时。
 */
@Slf4j
public final class RedisUtil {

    /** 熔断冷却时间（毫秒） */
    private static final long COOLDOWN_MS = 30_000;

    /** Redis 恢复可用前的时间戳 */
    private static volatile long failUntil = 0;

    private RedisUtil() {}

    /** 是否应跳过 Redis 操作 */
    public static boolean skipRedis() {
        return System.currentTimeMillis() < failUntil;
    }

    /** 标记 Redis 调用失败，进入熔断 */
    public static void markFailed() {
        failUntil = System.currentTimeMillis() + COOLDOWN_MS;
        log.warn("Redis 进入熔断，{} 秒内跳过所有 Redis 操作", COOLDOWN_MS / 1000);
    }

    /** 标记 Redis 调用成功，解除熔断 */
    public static void markSuccess() {
        if (failUntil > 0) {
            failUntil = 0;
            log.info("Redis 已恢复，解除熔断");
        }
    }
}
