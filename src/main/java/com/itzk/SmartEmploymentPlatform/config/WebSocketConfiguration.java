package com.itzk.SmartEmploymentPlatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 原生 WebSocket 端点导出配置（仅当使用 @ServerEndpoint 注解时启用）。
 * 当前项目使用 STOMP over WebSocket（WebSocketConfig），此配置为备用。
 */
@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
