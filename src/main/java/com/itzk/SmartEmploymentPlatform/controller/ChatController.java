package com.itzk.SmartEmploymentPlatform.controller;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Conversation;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ConversationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MessageVO;
import com.itzk.SmartEmploymentPlatform.service.ChatService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天模块 HTTP 接口。
 * 会话列表、创建会话、获取历史消息均走 HTTP REST；
 * 实时消息收发走 WebSocket STOMP（见 WsChatController）。
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 获取当前用户的所有会话列表。
     * 前端点击"消息"图标时调用，返回按最后消息时间倒序排列的会话列表。
     */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations() {
        Long userId = UserHolder.getUserId();
        return Result.success(chatService.getConversations(userId));
    }

    /**
     * 创建或获取与目标用户的会话。
     * 前端点击"立即沟通"时调用，传入 { "targetUserId": 对方ID }。
     * 返回的 id 后续在 STOMP /app/chat.send 中使用。
     */
    @PostMapping("/conversations")
    public Result<ConversationVO> createConversation(@RequestBody Map<String, Long> body) {
        Long userId = UserHolder.getUserId();
        Long targetUserId = body.get("targetUserId");
        if (targetUserId == null) {
            return Result.error("targetUserId 不能为空");
        }
        // 获取或创建会话（并发安全）
        Conversation conv = chatService.getOrCreateConversation(userId, targetUserId);
        log.info("getOrCreateConversation 返回: id={}, user1Id={}, user2Id={}", conv.getId(), conv.getUser1Id(), conv.getUser2Id());

        // 从当前用户视角构建 VO
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        boolean isUser1 = conv.getUser1Id().equals(userId);
        vo.setTargetUserId(isUser1 ? conv.getUser2Id() : conv.getUser1Id());
        vo.setTargetNickname(isUser1 ? conv.getUser2Nickname() : conv.getUser1Nickname());
        vo.setTargetAvatar(isUser1 ? conv.getUser2Avatar() : conv.getUser1Avatar());
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setUnreadCount(isUser1 ? conv.getUser1UnreadCount() : conv.getUser2UnreadCount());
        vo.setIsOnline(chatService.isUserOnline(vo.getTargetUserId()));
        return Result.success(vo);
    }

    /**
     * 获取某会话的历史消息。
     * 调用后自动将未读消息标记为已读，并清零该会话的未读计数。
     * 同时支持 /chat/messages/{id} 和 /chat/conversations/{id}/messages 两种路径。
     */
    @GetMapping({"/messages/{conversationId}", "/conversations/{conversationId}/messages"})
    public Result<List<MessageVO>> getMessages(@PathVariable Long conversationId) {
        Long userId = UserHolder.getUserId();
        return Result.success(chatService.getMessages(conversationId, userId));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public Result<?> deleteConversation(@PathVariable Long conversationId) {
        chatService.deleteConversation(conversationId, UserHolder.getUserId());
        return Result.success();
    }
}
