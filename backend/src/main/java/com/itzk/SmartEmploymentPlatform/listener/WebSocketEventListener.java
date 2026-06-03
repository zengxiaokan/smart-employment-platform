package com.itzk.SmartEmploymentPlatform.listener;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ConversationVO;
import com.itzk.SmartEmploymentPlatform.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * WebSocket 连接事件监听器。
 * 上线时：写入 Redis 在线状态 + 推送会话列表 + 批量推送离线未读消息。
 * 下线时：删除 Redis 在线状态、会话缓存、防抖 key。
 */
@Slf4j
@Component
public class WebSocketEventListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ChatService chatService;
    @Resource
    private SimpMessagingTemplate messagingTemplate;

    private static final String ONLINE_KEY_PREFIX = "online:user:";
    private static final String CONV_LIST_KEY_PREFIX = "conv:list:";
    private static final String PUSH_COOLDOWN_KEY_PREFIX = "conv:push:cooldown:";
    private static final Duration ONLINE_TTL = Duration.ofHours(12);
    /** 推送防抖窗口：同一用户在此时间内只推一次会话列表 */
    private static final Duration PUSH_COOLDOWN = Duration.ofSeconds(10);

    @EventListener
    public void onSessionConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if (user != null) {
            String userId = user.getName();

            // 在线状态写入
            try {
                stringRedisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, "1", ONLINE_TTL);
                log.info("用户 {} 上线", userId);
            } catch (Exception e) {
                log.warn("Redis 上线状态写入失败, userId={}, msg={}", userId, e.getMessage());
            }

            // 推送会话列表（防抖：10 秒内只推一次，解决 SockJS 多连接重复推送）
            String cooldownKey = PUSH_COOLDOWN_KEY_PREFIX + userId;
            try {
                Boolean alreadyPushed = stringRedisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", PUSH_COOLDOWN);
                if (Boolean.TRUE.equals(alreadyPushed)) {
                    List<ConversationVO> conversations = chatService.getConversations(Long.valueOf(userId));
                    messagingTemplate.convertAndSendToUser(
                            userId, "/queue/conversations", conversations);
                    log.info("已推送会话列表给用户 {}, 共 {} 条", userId, conversations.size());

                    // 推送离线期间收到的未读消息（延迟 1.5s，等待客户端 STOMP 订阅就绪）
                    Long userIdLong = Long.valueOf(userId);
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(1500);
                            List<Message> unreadMsgs = chatService.getLatestUnreadMessages(userIdLong);
                            if (unreadMsgs != null && !unreadMsgs.isEmpty()) {
                                for (Message msg : unreadMsgs) {
                                    messagingTemplate.convertAndSendToUser(
                                            userId, "/queue/chat", msg);
                                }
                                log.info("已推送 {} 条离线未读消息给用户 {}", unreadMsgs.size(), userId);
                            }
                        } catch (Exception e) {
                            log.warn("推送离线未读消息失败, userId={}, msg={}", userId, e.getMessage());
                        }
                    });
                } else {
                    log.info("用户 {} 已在 {} 秒内推送过，跳过", userId, PUSH_COOLDOWN.getSeconds());
                }
            } catch (Exception e) {
                log.warn("推送会话列表失败, userId={}, msg={}", userId, e.getMessage());
            }
        }
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if (user != null) {
            String userId = user.getName();
            try {
                // 清除在线状态
                stringRedisTemplate.delete(ONLINE_KEY_PREFIX + userId);
                // 清除会话列表缓存，保证下次上线拉取离线期间的新消息
                stringRedisTemplate.delete(CONV_LIST_KEY_PREFIX + userId);
                // 清除推送防抖 key，确保重连后能立即推送
                stringRedisTemplate.delete(PUSH_COOLDOWN_KEY_PREFIX + userId);
                log.info("用户 {} 下线", userId);
            } catch (Exception e) {
                log.warn("Redis 下线清理失败, userId={}, msg={}", userId, e.getMessage());
            }
        }
    }
}
