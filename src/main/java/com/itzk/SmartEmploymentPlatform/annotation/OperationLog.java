package com.itzk.SmartEmploymentPlatform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标注在需要记录操作日志的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作类型，如 USER_REGISTER / AUDIT_COMPANY */
    String action();

    /** 操作对象类型，如 user / company / job */
    String targetType();

    /** SpEL表达式，指向目标ID，如 "#result.data.id" / "#args[0]" */
    String targetId();

    /** 操作对象名称（SpEL表达式，可选），如 "#result.data.name" / "#register.username" */
    String targetName() default "";

    /** 操作备注（SpEL表达式，可选），如 "'审核通过，公司ID=' + #id" */
    String remark() default "";
}
