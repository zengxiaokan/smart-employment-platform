package com.itzk.SmartEmploymentPlatform.interceptor;

import com.itzk.SmartEmploymentPlatform.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器，在 HTTP 升级到 WebSocket 协议之前执行。
 * 从请求参数中提取 token 并校验 JWT，校验通过后将 userId 存入 attributes，
 * 后续由 UserHandshakeHandler 读取并转换为 Principal。
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 前端通过 ?token=xxx 传递 JWT
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败：token 为空");
                return false;
            }
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = ((Number) claims.get("userId")).longValue();
                // 存入 attributes，由 UserHandshakeHandler 读取
                attributes.put("userId", userId);
                log.info("WebSocket 握手成功，userId={}", userId);
                return true;
            } catch (Exception e) {
                log.warn("WebSocket 握手失败：token 无效");
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
