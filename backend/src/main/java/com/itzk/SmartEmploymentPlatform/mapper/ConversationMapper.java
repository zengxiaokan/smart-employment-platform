package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Conversation;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ConversationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMapper {

    /** 根据主键 ID 查询会话 */
    @Select("SELECT * FROM conversations WHERE id = #{id}")
    Conversation selectById(@Param("id") Long id);

    /** 根据两个用户 ID 查询会话（不区分顺序，user1 < user2 由调用方保证） */
    @Select("SELECT * FROM conversations WHERE (user1_id = #{user1} AND user2_id = #{user2}) OR (user1_id = #{user2} AND user2_id = #{user1})")
    Conversation selectByUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    /** 插入新会话（XML 实现，useGeneratedKeys） */
    int insert(Conversation conversation);

    /** 查询当前用户的所有会话列表，自动转换 target 视角（XML 实现，使用 CASE WHEN） */
    List<ConversationVO> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新最后消息 + 给接收方增加未读计数，一次 UPDATE 完成。
     * CASE WHEN 自动判断接收方是 user1 还是 user2，无需应用层 if-else。
     */
    @Update("UPDATE conversations SET last_message = #{content}, last_message_time = NOW(), updated_at = NOW(), " +
            "user1_unread_count = CASE WHEN user1_id = #{receiverId} THEN user1_unread_count + 1 ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN user2_id = #{receiverId} THEN user2_unread_count + 1 ELSE user2_unread_count END " +
            "WHERE id = #{id}")
    int updateLastMessageAndIncrUnread(@Param("id") Long id, @Param("content") String content, @Param("receiverId") Long receiverId);

    /** 清零 user1 的未读计数 */
    @Update("UPDATE conversations SET user1_unread_count = 0 WHERE id = #{id}")
    int clearUser1Unread(@Param("id") Long id);

    /** 清零 user2 的未读计数 */
    @Update("UPDATE conversations SET user2_unread_count = 0 WHERE id = #{id}")
    int clearUser2Unread(@Param("id") Long id);

    /** 删除会话 */
    @Delete("DELETE FROM conversations WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
