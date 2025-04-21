package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 消息类型：0-文本，1-图片，2-文件
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer readStatus;

    /**
     * 是否撤回：0-否，1-是
     */
    private Integer recalled;

    /**
     * 相关职位ID
     */
    private Long jobId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}