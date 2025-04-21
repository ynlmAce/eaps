package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组加入申请实体类
 */
@Data
@TableName("chat_group_application")
public class ChatGroupApplication {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 申请人用户ID
     */
    private Long applicantId;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 申请消息
     */
    private String applyMessage;
    
    /**
     * 申请状态：0-待处理，1-已接受，2-已拒绝
     */
    private Integer status;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理原因
     */
    private String handleReason;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 