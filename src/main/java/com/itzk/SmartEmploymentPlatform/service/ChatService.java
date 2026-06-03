package com.itzk.SmartEmploymentPlatform.service;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Conversation;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SendMessageDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ConversationVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MessageVO;

import java.util.List;

public interface ChatService {

    /**
     * 获取或创建两个用户之间的会话。
     * 内部保证 user1_id < user2_id，并通过 DuplicateKeyException 兜底处理并发创建。
     */
    Conversation getOrCreateConversation(Long user1Id, Long user2Id);

    /**
     * 保存消息并原子更新会话的 last_message 和未读计数（一条 SQL 完成）。
     */
    Message saveMessage(Long conversationId, Long senderId, SendMessageDTO dto);

    /**
     * 查询当前用户的所有会话列表（含在线状态）。
     */
    List<ConversationVO> getConversations(Long userId);

    /**
     * 查询某会话的历史消息，同时将未读消息标记为已读并清零会话未读计数。
     */
    List<MessageVO> getMessages(Long conversationId, Long userId);

    /**
     * 判断用户是否在线（Redis 查询，不可用时降级返回 false）。
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取用户所有会话中最新的未读消息（每个会话取最新一条）。
     * 用于用户登录后批量推送离线期间收到的消息。
     */
    List<Message> getLatestUnreadMessages(Long userId);

    /**
     * 删除会话及关联消息。
     */
    void deleteConversation(Long conversationId, Long userId);
}
