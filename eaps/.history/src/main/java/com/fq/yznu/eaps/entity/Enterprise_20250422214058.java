package com.fq.yznu.eaps.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业实体类
 */
@Data
@Entity
@Table(name = "enterprise")
public class Enterprise implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 企业法人
     */
    private String legalPerson;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 企业类型（1：国企，2：民企，3：外企，4：合资企业）
     */
    private Integer type;

    /**
     * 企业规模（1：小型，2：中型，3：大型）
     */
    private String scale;

    /**
     * 企业行业
     */
    private String industry;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 企业网站
     */
    private String website;

    /**
     * 企业Logo
     */
    private String logo;

    /**
     * 企业简介
     */
    private String introduction;

    /**
     * 成立时间
     */
    private String establishTime;

    /**
     * 注册资本
     */
    private String registeredCapital;

    /**
     * 员工数量
     */
    private Integer employeeCount;

    /**
     * 总评分
     */
    private Double totalRating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 审核状态（0：待审核，1：已通过，2：已拒绝）
     */
    private String status;

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
    private Integer deleted;
}