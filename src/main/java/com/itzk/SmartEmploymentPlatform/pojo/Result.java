package com.itzk.SmartEmploymentPlatform.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：1-成功，0或其它数字-失败 */
    private Integer code;

    /** 错误信息 */
    private String msg;

    /** 返回数据 */
    private T data;

    /**
     * 返回成功结果（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    /**
     * 返回成功结果（带数据）
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    /**
     * 返回错误结果
     */
    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
