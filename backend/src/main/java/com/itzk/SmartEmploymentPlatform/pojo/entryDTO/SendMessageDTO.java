package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;

/**
 * STOMP 消息发送 DTO，客户端通过 /app/chat.send 发送。
 * conversationId 由上一次 POST /chat/conversations 的返回值提供。
 */
@Data
public class SendMessageDTO {
    /** 会话 ID，首次由 POST /chat/conversations 返回 */
    private Long conversationId;
    /** 接收者用户 ID */
    private Long receiverId;
    /** 消息正文 */
    private String content;
    /** 消息类型：0=文本, 1=图片, 2=文件 */
    private Integer msgType;
    /** 客户端生成的消息临时 ID（UUID），用于 CHAT_ERROR 时精确匹配前端乐观渲染的临时消息 */
    private String clientMsgId;
}
