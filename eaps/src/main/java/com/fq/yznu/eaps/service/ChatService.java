package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.ChatConversation;
import com.fq.yznu.eaps.entity.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 发送消息
     *
     * @param message 消息实体
     * @return 发送结果
     */
    ChatMessage sendMessage(ChatMessage message);
    
    /**
     * 获取聊天消息历史
     *
     * @param conversationId 会话ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    Page<ChatMessage> getMessageHistory(Long conversationId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Map<String, Object>> getConversationList(Long userId);
    
    /**
     * 创建或获取会话
     *
     * @param userId 当前用户ID
     * @param targetId 目标用户ID
     * @param conversationType 会话类型：0-私聊，1-群聊
     * @param jobId 相关职位ID (可选，仅当是企业与学生关于某职位的会话时)
     * @return 会话信息
     */
    ChatConversation getOrCreateConversation(Long userId, Long targetId, Integer conversationType, Long jobId);
    
    /**
     * 标记消息为已读
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markMessagesAsRead(Long conversationId, Long userId);
    
    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, Long userId);
    
    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, Long userId);
    
    /**
     * 获取未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    int getUnreadMessageCount(Long userId);
} 