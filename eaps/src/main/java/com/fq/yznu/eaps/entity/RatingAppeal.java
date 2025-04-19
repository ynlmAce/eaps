package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评分申诉表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rating_appeal")
public class RatingAppeal extends BaseEntity {
    
    /**
     * 评分ID
     */
    private Long ratingId;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 申诉理由
     */
    private String appealReason;
    
    /**
     * 申诉证据
     */
    private String appealEvidence;
    
    /**
     * 申诉状态（0处理中 1已通过 2已驳回）
     */
    private Integer status;
    
    /**
     * 申诉时间
     */
    private LocalDateTime appealTime;
    
    /**
     * 处理人
     */
    private String handler;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
    /**
     * 处理备注
     */
    private String handleRemark;
} 