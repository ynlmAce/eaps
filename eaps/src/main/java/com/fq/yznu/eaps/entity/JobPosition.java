package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 职位信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_position")
public class JobPosition extends BaseEntity {
    
    /**
     * 企业用户ID
     */
    private Long enterpriseId;
    
    /**
     * 职位名称
     */
    private String positionName;
    
    /**
     * 职位类型（0全职 1兼职 2实习）
     */
    private Integer positionType;
    
    /**
     * 职位类别
     */
    private String positionCategory;
    
    /**
     * 招聘人数
     */
    private Integer recruitNumber;
    
    /**
     * 工作地点
     */
    private String workLocation;
    
    /**
     * 学历要求（0不限 1大专 2本科 3硕士 4博士）
     */
    private Integer educationRequirement;
    
    /**
     * 专业要求
     */
    private String majorRequirement;
    
    /**
     * 工作经验（0不限 1应届生 2一年以内 3一到三年 4三到五年 5五年以上）
     */
    private Integer experienceRequirement;
    
    /**
     * 薪资下限
     */
    private BigDecimal salaryLower;
    
    /**
     * 薪资上限
     */
    private BigDecimal salaryUpper;
    
    /**
     * 薪资单位（0元/月 1元/天 2元/小时）
     */
    private Integer salaryUnit;
    
    /**
     * 职位福利（多个用逗号分隔）
     */
    private String benefits;
    
    /**
     * 职位描述
     */
    private String description;
    
    /**
     * 职位要求
     */
    private String requirement;
    
    /**
     * 职位状态（0草稿 1发布中 2已暂停 3已结束）
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadlineTime;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 申请人数
     */
    private Integer applyCount;
    
    /**
     * 置顶（0否 1是）
     */
    private Integer isTop;
    
    /**
     * 推荐（0否 1是）
     */
    private Integer isRecommend;
    
    /**
     * 紧急招聘（0否 1是）
     */
    private Integer isUrgent;
    
    /**
     * 审核状态（0未审核 1已审核 2审核未通过）
     */
    private Integer verifyStatus;
    
    /**
     * 审核人
     */
    private String verifier;
    
    /**
     * 审核时间
     */
    private LocalDateTime verifyTime;
    
    /**
     * 审核意见
     */
    private String verifyComment;
} 