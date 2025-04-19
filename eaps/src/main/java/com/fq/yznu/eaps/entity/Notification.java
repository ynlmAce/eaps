package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收用户ID
     */
    private Long userId;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型：1-系统通知、2-审核通知、3-私信通知
     */
    private Integer type;
    
    /**
     * 关联ID（如推广ID、申请ID等）
     */
    private Long relatedId;
    
    /**
     * 关联类型（如promotion、application等）
     */
    private String relatedType;
    
    /**
     * 是否已读：0-未读、1-已读
     */
    private Integer isRead;
    
    /**
     * 已读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    
    /**
     * 状态：0-待发送、1-已发送、2-发送失败
     */
    private Integer status;
    
    /**
     * 逻辑删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
} 