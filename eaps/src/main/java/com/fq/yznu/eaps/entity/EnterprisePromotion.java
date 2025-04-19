package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业宣传信息实体类
 */
@Data
@TableName("enterprise_promotion")
public class EnterprisePromotion implements Serializable {

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
     * 企业名称（冗余字段）
     */
    @TableField(exist = false)
    private String enterpriseName;

    /**
     * 宣传类型：1-企业介绍、2-企业文化、3-企业环境、4-员工福利
     */
    private Integer type;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 附件URL，多个附件以逗号分隔
     */
    private String attachmentUrls;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 状态：0-待审核、1-已通过、2-已拒绝
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String reviewComments;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 创建人ID
     */
    private Long creatorId;

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

    /**
     * 是否置顶：0-否、1-是
     */
    private Integer isTop;

    /**
     * 宣讲会时间（当type=2时使用）
     */
    private LocalDateTime eventTime;

    /**
     * 宣讲会地点（当type=2时使用）
     */
    private String eventLocation;

    /**
     * 宣讲会日程安排（当type=2时使用）
     */
    private String eventAgenda;

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
     * 优先级：数字越大优先级越高
     */
    private Integer priority;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expiredTime;
} 