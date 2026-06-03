package com.itzk.SmartEmploymentPlatform.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 自定义握手处理器，将 HandshakeInterceptor 存入 attributes 的 userId 转换为 Spring 的 Principal。
 * 这一步是 WebSocket 认证的关键：attributes 里的 userId 不会被 Spring 自动识别为当前用户，
 * 必须在 determineUser() 中手动包装成 StompPrincipal 返回，之后 @MessageMapping 方法的 Principal 参数才能拿到值。
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                       WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        // 1. 从 attributes 中取出之前拦截器存入的 userId
        Long userId = (Long) attributes.get("userId");
        if (userId == null) {
            return null;
        }
        return new StompPrincipal(userId.toString());
    }
}
