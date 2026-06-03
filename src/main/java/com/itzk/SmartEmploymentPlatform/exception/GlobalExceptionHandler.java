package com.itzk.SmartEmploymentPlatform.exception;


import com.itzk.SmartEmploymentPlatform.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理所有Controller层抛出的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理唯一键冲突异常（重复投递、重复注册等）
     */

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CREATED)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("唯一键冲突异常：");
        
        // 根据不同的唯一索引返回不同的提示信息
        String message = e.getMessage();

        if (message.contains("uk_job_applicant")) {
            return Result.error("您已经投递过这个岗位了，请勿重复投递");
        } else if (message.contains("uk_username")) {
            return Result.error("用户名已存在");
        } else if (message.contains("uk_phone")) {
            return Result.error("手机号已被注册");
        }else if (message.contains("uk_email")) {
            return Result.error("邮箱已被注册");
        }
        
        // 默认提示
        return Result.error("操作失败，数据已存在");
    }


    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleValidationException(Exception e) {
        log.error("参数校验异常：", e);
        String message = "参数校验失败";
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        
        return Result.error(message);
    }

    /**
     * 处理业务异常（自定义）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统繁忙，请稍后再试");
    }
}