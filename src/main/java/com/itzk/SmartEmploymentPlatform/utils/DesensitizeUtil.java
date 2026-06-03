package com.itzk.SmartEmploymentPlatform.utils;

/**
 * 数据脱敏工具 —— 简历列表展示用
 */
public final class DesensitizeUtil {

    private DesensitizeUtil() {}

    /** 手机号脱敏：138****1234 */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 邮箱脱敏：t***@example.com */
    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String prefix = email.substring(0, at);
        if (prefix.length() <= 1) {
            return "*" + email.substring(at);
        }
        return prefix.charAt(0) + "***" + email.substring(at);
    }

    /** 姓名脱敏：两位→张*  三位及以上→张*三  单字→* */
    public static String name(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append('*');
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }
}
