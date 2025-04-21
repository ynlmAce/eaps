package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位实体类
 */
@Data
@TableName("job_position")
public class JobPosition implements Serializable {

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
     * 职位名称
     */
    private String positionName;
    
    /**
     * 职位类型
     */
    private String positionType;
    
    /**
     * 职位描述
     */
    private String description;
    
    /**
     * 职位要求
     */
    private String requirement;
    
    /**
     * 工作地点
     */
    private String location;
    
    /**
     * 工作城市
     */
    private String city;
    
    /**
     * 薪资范围（起始）
     */
    private Integer salaryMin;
    
    /**
     * 薪资范围（最高）
     */
    private Integer salaryMax;
    
    /**
     * 薪资单位（0：元/月，1：元/年）
     */
    private Integer salaryUnit;
    
    /**
     * 学历要求（1：专科，2：本科，3：硕士，4：博士）
     */
    private Integer educationRequirement;
    
    /**
     * 经验要求（0：不限，1：应届生，2：1-3年，3：3-5年，4：5年以上）
     */
    private Integer experienceRequirement;
    
    /**
     * 招聘人数
     */
    private Integer recruitNumber;
    
    /**
     * 申请人数
     */
    private Integer applyCount;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 状态（0：待审核，1：已发布，2：已下线，3：审核未通过）
     */
    private Integer status;
    
    /**
     * 审核意见
     */
    private String reviewComment;
    
    /**
     * 是否置顶（0：否，1：是）
     */
    private Integer isTop;
    
    /**
     * 置顶截止时间
     */
    private LocalDateTime topEndTime;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
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