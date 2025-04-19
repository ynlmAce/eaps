package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天会话表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class ChatConversation extends BaseEntity {
    
    /**
     * 会话类型（0私聊 1群聊）
     */
    private Integer conversationType;
    
    /**
     * 会话名称（群聊时有值）
     */
    private String conversationName;
    
    /**
     * 会话创建者ID
     */
    private Long creatorId;
    
    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 会话状态（0正常 1置顶 2静音 3归档）
     */
    private Integer status;
} 