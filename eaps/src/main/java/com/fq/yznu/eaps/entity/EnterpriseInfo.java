package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业信息实体类
 */
@Data
@TableName("enterprise_info")
public class EnterpriseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 企业类型：1-国企、2-民营、3-外企、4-合资
     */
    private Integer enterpriseType;

    /**
     * 企业规模：1-50人以下、2-50-200人、3-200-500人、4-500-1000人、5-1000人以上
     */
    private Integer scale;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 企业网站
     */
    private String website;

    /**
     * 企业描述
     */
    private String description;

    /**
     * 企业Logo URL
     */
    private String logoUrl;

    /**
     * 营业执照图片URL
     */
    private String licenseUrl;

    /**
     * 审核状态：0-待审核、1-已通过、2-已拒绝
     */
    private Integer verifyStatus;

    /**
     * 审核意见
     */
    private String verifyComment;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyTime;

    /**
     * 审核人ID
     */
    private Long verifierId;

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
     * 逻辑删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
} 