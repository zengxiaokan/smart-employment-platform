package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话列表展示对象，从当前登录用户视角转换。
 * targetUserId / targetNickname / targetAvatar 始终是"对方"的信息。
 */
@Data
public class ConversationVO {
    private Long id;
    /** 对方的用户 ID */
    private Long targetUserId;
    /** 对方的昵称 */
    private String targetNickname;
    /** 对方的头像 */
    private String targetAvatar;
    /** 最后一条消息内容 */
    private String lastMessage;
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;
    /** 当前用户的未读消息数 */
    private Integer unreadCount;
    /** 对方是否在线 */
    private Boolean isOnline;
}
