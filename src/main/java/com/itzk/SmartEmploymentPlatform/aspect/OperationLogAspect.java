package com.itzk.SmartEmploymentPlatform.aspect;

import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.mapper.OperationLogMapper;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private OperationLogMapper operationLogMapper;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = discoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            context.setVariable("args", args);
            context.setVariable("result", result);

            Long targetId = parseExpression(operationLog.targetId(), context, Long.class);
            if (targetId == null) {
                return result;
            }

            // 自操作场景（如注册）：未登录状态下，操作者即新创建对象本身
            Long userId = UserHolder.getUserId();
            String username = UserHolder.getUsername();
            if (userId == null) {
                userId = targetId;
            }
            if (username == null && !operationLog.targetName().isEmpty()) {
                username = parseExpression(operationLog.targetName(), context, String.class);
            }

            context.setVariable("userId", userId);
            context.setVariable("username", username);

            com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog log = new com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog();
            log.setUserId(userId);
            log.setOperatorName(username);
            log.setAction(operationLog.action());
            log.setTargetType(operationLog.targetType());
            log.setTargetId(targetId);

            if (!operationLog.targetName().isEmpty()) {
                log.setTargetName(parseExpression(operationLog.targetName(), context, String.class));
            }

            String ip = getClientIp();
            log.setIpAddress(ip);

            if (!operationLog.remark().isEmpty()) {
                String remark = parseExpression(operationLog.remark(), context, String.class);
                log.setRemark(remark);
            }

            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务
        }

        return result;
    }

    private <T> T parseExpression(String expression, EvaluationContext context, Class<T> clazz) {
        try {
            Expression expr = parser.parseExpression(expression);
            return expr.getValue(context, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
