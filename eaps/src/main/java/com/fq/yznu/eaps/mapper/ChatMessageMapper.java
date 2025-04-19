package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页查询会话消息
     *
     * @param page 分页参数
     * @param conversationId 会话ID
     * @return 消息列表
     */
    IPage<ChatMessage> selectMessagesByConversationId(Page<ChatMessage> page, @Param("conversationId") Long conversationId);
    
    /**
     * 更新消息为已读状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET status = 1, read_time = NOW() WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND status = 0")
    int updateMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    /**
     * 获取用户未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    int countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * 更新消息为撤回状态
     *
     * @param messageId 消息ID
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET is_recalled = 1 WHERE id = #{messageId}")
    int recallMessage(@Param("messageId") Long messageId);
} 