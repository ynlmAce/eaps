package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业评分实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_enterprise_rating")
public class EnterpriseRating implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 企业ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 学生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 企业回复
     */
    private String reply;

    /**
     * 回复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    /**
     * 有用数量
     */
    private Integer helpfulCount;

    /**
     * 状态（0-待审核，1-已通过，2-未通过，3-已申诉）
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /**
     * 申诉内容
     */
    private String appealContent;

    /**
     * 申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;

    /**
     * 申诉状态（0-待处理，1-已驳回，2-已通过）
     */
    private Integer appealStatus;

    /**
     * 申诉处理意见
     */
    private String appealReviewComment;

    /**
     * 申诉处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;

    /**
     * 已标记有用的用户ID列表（JSON数组）
     */
    @JsonIgnore
    private String helpfulUsers;

    /**
     * 是否删除
     */
    @TableLogic
    @JsonIgnore
    private Boolean deleted;

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

    /**
     * 乐观锁
     */
    @TableField("version")
    @JsonIgnore
    private Integer version;
} 