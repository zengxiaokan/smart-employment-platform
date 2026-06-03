package com.itzk.SmartEmploymentPlatform.config;

import com.itzk.SmartEmploymentPlatform.interceptor.WebSocketHandshakeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket 配置。
 * 启用消息代理，注册 /ws 端点（SockJS 兼容），并绑定握手拦截器和自定义 HandshakeHandler。
 *
 * 消息流向：
 * 客户端 → /app/chat.send → @MessageMapping("/chat.send") → 服务端处理
 * 服务端 → /user/{userId}/queue/chat → 客户端订阅 /user/queue/chat
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 内置消息代理，处理 /topic（广播）和 /queue（点对点）前缀
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发消息时目标前缀
        registry.setApplicationDestinationPrefixes("/app");
        // convertAndSendToUser 的前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 原生 WebSocket（单连接，无 SockJS 兼容开销，推荐前端优先使用）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new UserHandshakeHandler())
                .addInterceptors(handshakeInterceptor);

        // SockJS 降级端点（兼容老浏览器，会创建多条连接探测协议）
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new UserHandshakeHandler())
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }
}
