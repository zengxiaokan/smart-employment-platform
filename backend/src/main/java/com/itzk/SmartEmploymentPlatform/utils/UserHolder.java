package com.itzk.SmartEmploymentPlatform.utils;

import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfo;

/**
 * ThreadLocal 工具类
 * 用于存储和获取当前登录用户信息
 * 线程安全，无内存泄漏风险
 */
public class UserHolder {

    // 私有静态 ThreadLocal 实例，使用完必须 remove！
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 保存当前用户信息
     * @param userInfo 当前登录用户
     */
    public static void setUser(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    /**
     * 获取当前用户信息
     * @return 当前登录用户
     */
    public static UserInfo getUserInfo() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户名
     * @return 用户名
     */
    public static String getUsername() {
        UserInfo user = getUserInfo();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户状态
     */
    public static Byte getStatus() {
        UserInfo user = getUserInfo();
        return user != null ? user.getStatus() : null;
    }

    /**
     * 获取当前用户角色
     * @return 角色 0求职者 1HR 2管理员
     */
    public static Byte getRole() {
        UserInfo user = getUserInfo();
        return user != null ? user.getRole() : null;
    }


    public static Long getCompanyId() {
        UserInfo user = getUserInfo();
        return user != null ? user.getCompanyId() : null;
    }

    /**
     * 清除当前用户信息
     * 【非常重要】必须在请求结束后调用，否则会导致内存泄漏
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}