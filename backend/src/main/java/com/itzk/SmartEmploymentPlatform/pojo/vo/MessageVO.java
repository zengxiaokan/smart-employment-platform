package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息展示对象，不包含 receiverId（前端可通过 conversationId 推导）。
 */
@Data
public class MessageVO {
    private Long id;
    /** 所属会话 ID */
    private Long conversationId;
    /** 发送者用户 ID，前端据此判断消息在左侧还是右侧 */
    private Long senderId;
    /** 消息内容 */
    private String content;
    /** 消息类型：0=文本, 1=图片, 2=文件 */
    private Integer msgType;
    /** 是否已读：0=未读, 1=已读 */
    private Integer isRead;
    /** 发送时间 */
    private LocalDateTime sentAt;
}
