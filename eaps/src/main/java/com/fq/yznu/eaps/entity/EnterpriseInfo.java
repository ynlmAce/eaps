package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

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
     * 企业类型（1：国企，2：民企，3：外企，4：合资企业）
     */
    private Integer enterpriseType;

    /**
     * 企业规模（1：小型，2：中型，3：大型）
     */
    private Integer enterpriseSize;

    /**
     * 企业行业
     */
    private String industry;

    /**
     * 企业地址
     */
    private String address;

    /**
     * 企业电话
     */
    private String phone;

    /**
     * 企业邮箱
     */
    private String email;

    /**
     * 企业网站
     */
    private String website;

    /**
     * 营业执照路径
     */
    private String licensePath;

    /**
     * 企业Logo路径
     */
    private String logoPath;

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
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 企业联系人
     */
    private String contactPerson;

    /**
     * 联系人职位
     */
    private String contactPosition;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 审核状态（0：待审核，1：已通过，2：已拒绝）
     */
    private Integer verifyStatus;

    /**
     * 审核人
     */
    private String verifier;

    /**
     * 审核时间
     */
    private String verifyTime;

    /**
     * 审核意见
     */
    private String verifyComment;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 逻辑删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
}