package com.itzk.SmartEmploymentPlatform.controller;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SendMessageDTO;
import com.itzk.SmartEmploymentPlatform.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天 WebSocket STOMP 消息控制器。
 * 处理客户端发往 /app/chat.send 的消息，保存后仅推送给接收方。
 * 发送方由前端乐观渲染，不额外回显，避免重复消息。
 *
 * 消息流向：
 * 客户端 → STOMP SEND /app/chat.send → 本方法 → 保存到数据库
 * → convertAndSendToUser 推送到 /user/{receiverId}/queue/chat（接收方）
 * → 发送方前端在发出消息时即刻渲染，不等待推送
 */
@Slf4j
@Controller
public class WsChatController {

    @Resource
    private ChatService chatService;
    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理聊天消息发送。
     * 如果 dto.conversationId 已有值（前端在 POST /conversations 后拿到），直接复用，省一次 SELECT。
     * 如果为空（容错：前端未调用 POST /conversations），则自动创建会话。
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageDTO dto, Principal principal) {
        if (principal == null) {
            log.warn("收到未经认证的 STOMP 消息，已忽略");
            return;
        }
        Long senderId = Long.valueOf(principal.getName());

        Long conversationId = dto.getConversationId();
        if (conversationId == null) {
            conversationId = chatService.getOrCreateConversation(senderId, dto.getReceiverId()).getId();
        }

        try {
            Message saved = chatService.saveMessage(conversationId, senderId, dto);
            messagingTemplate.convertAndSendToUser(
                    dto.getReceiverId().toString(), "/queue/chat", saved);
        } catch (Exception e) {
            log.error("消息保存失败, senderId={}, conversationId={}", senderId, conversationId, e);
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("type", "CHAT_ERROR");
            errorMsg.put("conversationId", conversationId);
            errorMsg.put("clientMsgId", dto.getClientMsgId());
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(), "/queue/chat", errorMsg);
        }
    }
}
