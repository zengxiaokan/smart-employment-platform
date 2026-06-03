package com.itzk.SmartEmploymentPlatform.interceptor;

import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfo;
import com.itzk.SmartEmploymentPlatform.utils.JwtUtil;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor{

    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 仅放行无需登录的接口：登录、注册、验证码、忘记密码
        if ("/login/loginpwd".equals(requestURI) || "/login/loginphone".equals(requestURI)
                || "/login/code".equals(requestURI) || "/login/register".equals(requestURI)
                || "/login/forgot-pwd/code".equals(requestURI)
                || "/login/forgot-pwd/verify".equals(requestURI)
                || "/login/forgot-pwd/reset".equals(requestURI)) {
            log.info("无需登录，放行: {}", requestURI);
            return true;
        }


        String token = request.getHeader("token");

        if(token == null || token.isEmpty()){
            log.info("令牌为空,响应401");
            response.setStatus(401);
            return false;
        }

        //如果token存在,校验令牌
        if (!jwtUtil.validate(token)) {
            response.setStatus(401);
            log.info("不合法令牌");
            return false;
        }

        //从令牌中取出信息
        Claims claims = jwtUtil.parseToken(token);
        Long id = ((Number)claims.get("userId")).longValue();
        String username = (String) claims.get("username");
        Byte role = ((Number)claims.get("role")).byteValue();
        Byte status = ((Number)claims.get("status")).byteValue();
        Long companyId = ((Number)claims.get("companyId")).longValue();

        if (requestURI.startsWith("/admin/")) {
            // 管理员接口：允许公司管理(2)和超级管理员(3)访问
            if (role != 2 && role != 3) {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问管理员接口\"}");
                return false;
            }
        } else if (requestURI.startsWith("/hr/")) {
            // HR接口：只允许身份=1访问
            if (role == 0) {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问HR接口\"}");
                return false;
            }
        } else if (requestURI.startsWith("/user/")) {
            // 求职者接口：只允许身份=0访问
            if (role == 1 ) {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问求职者接口\"}");
                return false;
            }
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(id);
        userInfo.setUsername(username);
        userInfo.setRole(role);
        userInfo.setStatus(status);
        userInfo.setCompanyId(companyId);

        UserHolder.setUser(userInfo);



        log.info("校验成功，放行用户id为{}",id);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.remove();
    }
}
