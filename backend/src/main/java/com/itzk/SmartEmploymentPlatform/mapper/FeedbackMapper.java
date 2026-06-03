package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Feedback;
import com.itzk.SmartEmploymentPlatform.pojo.vo.FeedbackVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface FeedbackMapper {

    int insert(Feedback feedback);

    int updateReply(@Param("id") Long id, @Param("reply") String reply);

    @Update("UPDATE feedbacks SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM feedbacks WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id, type, title, content, images, status, reply, target_type, target_id, " +
            "DATE_FORMAT(replied_at, '%Y-%m-%d %H:%i:%s') AS repliedAt, " +
            "DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS createdAt, " +
            "DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i:%s') AS updatedAt " +
            "FROM feedbacks WHERE id = #{id}")
    Feedback selectById(@Param("id") Long id);

    @Select("SELECT id, user_id, type, title, content, images, status, reply, target_type, target_id, " +
            "DATE_FORMAT(replied_at, '%Y-%m-%d %H:%i:%s') AS repliedAt, " +
            "DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS createdAt, " +
            "DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i:%s') AS updatedAt " +
            "FROM feedbacks WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Feedback> selectByUserId(@Param("userId") Long userId);

    List<FeedbackVO> selectAdminList(@Param("type") Integer type, @Param("status") Integer status);
}
