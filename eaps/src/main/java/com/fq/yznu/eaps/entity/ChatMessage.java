package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天消息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends BaseEntity {
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息类型（0文本 1图片 2文件 3系统消息）
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 文件路径（图片或文件类型时有值）
     */
    private String filePath;
    
    /**
     * 文件名（文件类型时有值）
     */
    private String fileName;
    
    /**
     * 文件大小（文件类型时有值）
     */
    private Long fileSize;
    
    /**
     * 消息状态（0未读 1已读 2已删除）
     */
    private Integer status;
    
    /**
     * 是否撤回（0否 1是）
     */
    private Integer isRecalled;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 读取时间
     */
    private LocalDateTime readTime;
    
    /**
     * 相关职位ID
     */
    private Long jobId;
} 