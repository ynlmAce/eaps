package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 企业评分实体类
 */
@Data
@TableName("enterprise_rating")
public class EnterpriseRating implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 总评分（1-5）
     */
    private BigDecimal rating;

    /**
     * 工作环境评分（1-5）
     */
    private Double environmentRating;

    /**
     * 薪资福利评分（1-5）
     */
    private Double salaryRating;

    /**
     * 成长空间评分（1-5）
     */
    private Double developmentRating;

    /**
     * 企业文化评分（1-5）
     */
    private Double cultureRating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    /**
     * 评价图片（JSON数组）
     */
    private String images;

    /**
     * 是否匿名（0：否，1：是）
     */
    private Integer anonymous;

    /**
     * 企业回复
     */
    private String reply;

    /**
     * 回复时间
     */
    private Date replyTime;

    /**
     * 有用数量
     */
    private Integer helpfulCount;

    /**
     * 状态（0：待审核，1：已通过，2：已拒绝）
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 申诉状态（0：未申诉，1：申诉中，2：申诉成功，3：申诉失败）
     */
    private Integer appealStatus;

    /**
     * 申诉内容
     */
    private String appealComment;

    /**
     * 申诉证据
     */
    private String appealEvidence;

    /**
     * 申诉时间
     */
    private LocalDateTime appealTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
}