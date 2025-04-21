package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位申请实体类
 */
@Data
@TableName("job_application")
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 职位ID
     */
    private Long jobId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 简历类型（0：在线填写，1：附件上传）
     */
    private Integer resumeType;
    
    /**
     * 简历内容（在线填写时使用）
     */
    private String resumeContent;
    
    /**
     * 简历路径（附件上传时使用）
     */
    private String resumePath;
    
    /**
     * 自我介绍
     */
    private String selfIntroduction;
    
    /**
     * 申请状态（0：已申请，1：已查看，2：面试中，3：已录用，4：已拒绝，5：已撤销）
     */
    private Integer status;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 查看时间
     */
    private LocalDateTime viewTime;
    
    /**
     * 企业反馈
     */
    private String feedback;
    
    /**
     * 反馈时间
     */
    private LocalDateTime feedbackTime;
    
    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;
    
    /**
     * 面试地点
     */
    private String interviewLocation;
    
    /**
     * 面试联系人
     */
    private String interviewContact;
    
    /**
     * 面试联系电话
     */
    private String interviewPhone;
    
    /**
     * 面试备注
     */
    private String interviewRemark;
    
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