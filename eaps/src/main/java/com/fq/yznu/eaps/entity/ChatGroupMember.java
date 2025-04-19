package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天群组成员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_group_member")
public class ChatGroupMember extends BaseEntity {
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称（在群中的昵称）
     */
    private String nickname;
    
    /**
     * 成员类型（0普通成员 1管理员 2群主）
     */
    private Integer memberType;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 禁言结束时间（为空表示未被禁言）
     */
    private LocalDateTime muteEndTime;
    
    /**
     * 成员状态（0正常 1已退出）
     */
    private Integer status;
} 