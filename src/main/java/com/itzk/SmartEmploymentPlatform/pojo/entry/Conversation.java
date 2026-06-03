package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话实体，对应 conversations 表。
 * 每两个用户之间最多一条会话记录，user1_id 始终小于 user2_id（由 getOrCreateConversation 保证）。
 * last_message 和 last_message_time 由发送消息时更新，用于会话列表排序和预览。
 * user1_unread_count / user2_unread_count 分别记录两端的未读数。
 */
@Data
public class Conversation {
    private Long id;
    /** 较小用户的 ID，保证 user1_id < user2_id，方便唯一索引去重 */
    private Long user1Id;
    /** 较大用户的 ID */
    private Long user2Id;
    private String user1Nickname;
    private String user1Avatar;
    private String user2Nickname;
    private String user2Avatar;
    /** 会话最后一条消息内容，用于列表预览 */
    private String lastMessage;
    /** 最后一条消息时间，用于排序 */
    private LocalDateTime lastMessageTime;
    /** user1 未读消息数 */
    private Integer user1UnreadCount;
    /** user2 未读消息数 */
    private Integer user2UnreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
