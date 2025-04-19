package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天群组加入申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_group_join_request")
public class ChatGroupJoinRequest extends BaseEntity {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 申请人ID
     */
    private Long userId;
    
    /**
     * 申请消息
     */
    private String message;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理结果（0待处理 1已批准 2已拒绝）
     */
    private Integer status;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
} 