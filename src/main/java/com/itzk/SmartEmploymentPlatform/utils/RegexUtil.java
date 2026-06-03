package com.itzk.SmartEmploymentPlatform.utils;

import java.util.regex.Pattern;

/**
 * 正则校验工具
 */
public final class RegexUtil {

    private RegexUtil() {}

    /** 手机号：1[3-9] 开头，11 位数字 */
    // 覆盖2026年所有已发布的手机号段
    // 你的写法
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱 */
    private static final Pattern EMAIL =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /** 用户名：3-30 位字母/数字/下划线，不允许中文 */
    private static final Pattern USERNAME =
            Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    /** 密码：6-20 位字母/数字/下划线 */
    private static final Pattern PASSWORD =
            Pattern.compile("^[a-zA-Z0-9_!@#$%^&*()-+=]{6,20}$");

    public static boolean isUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isPhone(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }

    public static boolean isEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }
}
