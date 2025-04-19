package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 聊天群组表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_group")
public class ChatGroup extends BaseEntity {
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群组类型（0学习交流 1就业经验 2行业交流）
     */
    private Integer groupType;
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 群组创建者ID
     */
    private Long creatorId;
    
    /**
     * 群组简介
     */
    private String introduction;
    
    /**
     * 群组公告
     */
    private String announcement;
    
    /**
     * 群组人数上限
     */
    private Integer maxMemberCount;
    
    /**
     * 当前成员数
     */
    private Integer currentMemberCount;
    
    /**
     * 群组头像
     */
    private String avatar;
    
    /**
     * 是否需要审核（0否 1是）
     */
    private Integer needVerify;
    
    /**
     * 群组状态（0正常 1已解散）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 