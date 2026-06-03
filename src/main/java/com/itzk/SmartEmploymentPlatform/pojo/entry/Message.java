package com.itzk.SmartEmploymentPlatform.pojo.entry;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体，对应 messages 表。
 * msgType: 0=文字, 1=系统通知。
 * isRead: 0=未读, 1=已读。
 */
@Data
public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    /** 消息类型：0=文字, 1=系统通知 */
    private Integer msgType;
    /** 是否已读：0=未读, 1=已读 */
    private Integer isRead;
    private LocalDateTime sentAt;
}
