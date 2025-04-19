package com.fq.yznu.eaps.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业推广详情视图对象
 */
@Data
public class PromotionDetailVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;
    
    /**
     * 企业联系电话
     */
    private String enterprisePhone;
    
    /**
     * 企业邮箱
     */
    private String enterpriseEmail;
    
    /**
     * 企业Logo
     */
    private String enterpriseLogo;
    
    /**
     * 企业简介
     */
    private String enterpriseDescription;

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
     * 附件列表
     */
    private String[] attachments;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 状态：0-待审核、1-已通过、2-已拒绝
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;

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
     * 是否置顶：0-否、1-是
     */
    private Integer isTop;

    /**
     * 宣讲会时间（当type=2时使用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
} 