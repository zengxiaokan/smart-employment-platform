package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Message;
import com.itzk.SmartEmploymentPlatform.pojo.vo.MessageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {

    /** 插入新消息（XML 实现，useGeneratedKeys） */
    int insert(Message message);

    /** 查询某会话的所有消息，按时间正序（XML 实现） */
    List<MessageVO> selectByConversationId(@Param("conversationId") Long conversationId);

    /** 将某个会话中发给当前用户的消息标记为已读 */
    @Update("UPDATE messages SET is_read = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND is_read = 0")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /** 将发给当前用户的所有消息标记为已读（预留） */
    @Update("UPDATE messages SET is_read = 1 WHERE receiver_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);

    /** 查询当前用户每个会话中最新的未读消息（登录后批量推送用） */
    List<Message> selectLatestUnreadByReceiver(@Param("receiverId") Long receiverId);

    /** 删除 N 天前的普通聊天消息（保留系统通知 msg_type=1） */
    @Delete("DELETE FROM messages WHERE msg_type != 1 AND sent_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOlderThan(@Param("days") int days);

    /** 查询系统通知（管理员） */
    List<Message> selectAnnouncements(@Param("senderId") Long senderId);

    /** 查询最新 N 条系统通知（用户端） */
    List<Message> selectLatestAnnouncements(@Param("limit") int limit);

    /** 更新消息内容 */
    @Update("UPDATE messages SET content = #{content} WHERE id = #{id}")
    int updateContent(@Param("id") Long id, @Param("content") String content);

    /** 按 ID 删除消息 */
    @Delete("DELETE FROM messages WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 按会话 ID 删除所有消息 */
    @Delete("DELETE FROM messages WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") Long conversationId);

    /** 按 ID 查询消息 */
    @Select("SELECT * FROM messages WHERE id = #{id}")
    Message selectById(@Param("id") Long id);
}
