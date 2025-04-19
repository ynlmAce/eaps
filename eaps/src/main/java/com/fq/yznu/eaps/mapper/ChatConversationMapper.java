package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Map<String, Object>> selectConversationsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取两个用户之间的私聊会话
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @param jobId 相关职位ID (可选)
     * @return 会话信息
     */
    @Select("SELECT * FROM chat_conversation WHERE " +
            "(creator_id = #{userId1} AND target_id = #{userId2} OR creator_id = #{userId2} AND target_id = #{userId1}) " +
            "AND conversation_type = 0 AND status != -1 " +
            "AND (job_id = #{jobId} OR job_id IS NULL) LIMIT 1")
    ChatConversation selectPrivateConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("jobId") Long jobId);
    
    /**
     * 更新会话最后一条消息信息
     *
     * @param conversationId 会话ID
     * @param lastMessageId 最后一条消息ID
     * @param lastMessageContent 最后一条消息内容
     * @return 影响行数
     */
    @Update("UPDATE chat_conversation SET last_message_id = #{lastMessageId}, " +
            "last_message_content = #{lastMessageContent}, last_message_time = NOW() " +
            "WHERE id = #{conversationId}")
    int updateLastMessage(
            @Param("conversationId") Long conversationId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("lastMessageContent") String lastMessageContent);
    
    /**
     * 增加会话未读消息数
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    @Update("UPDATE chat_conversation SET unread_count = unread_count + 1 WHERE id = #{conversationId}")
    int incrementUnreadCount(@Param("conversationId") Long conversationId);
    
    /**
     * 重置会话未读消息数
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    @Update("UPDATE chat_conversation SET unread_count = 0 WHERE id = #{conversationId}")
    int resetUnreadCount(@Param("conversationId") Long conversationId);
} 