package com.itzk.SmartEmploymentPlatform.config;

import java.security.Principal;

/**
 * STOMP 协议的 Principal 实现，用于在 WebSocket 会话中标识当前用户。
 * Spring 的 SimpMessagingTemplate.convertAndSendToUser() 依赖 Principal.getName() 来定位目标用户。
 */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
