package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 职位申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_application")
public class JobApplication extends BaseEntity {
    
    /**
     * 职位ID
     */
    private Long jobId;
    
    /**
     * 学生用户ID
     */
    private Long studentId;
    
    /**
     * 企业用户ID
     */
    private Long enterpriseId;
    
    /**
     * 简历类型（0在线填写 1附件上传）
     */
    private Integer resumeType;
    
    /**
     * 简历路径（附件上传时有值）
     */
    private String resumePath;
    
    /**
     * 简历内容（在线填写时有值，JSON格式）
     */
    private String resumeContent;
    
    /**
     * 求职信
     */
    private String coverLetter;
    
    /**
     * 申请状态（0已申请 1已查看 2面试中 3已录用 4已拒绝）
     */
    private Integer status;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 企业查看时间
     */
    private LocalDateTime viewTime;
    
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
     * 企业反馈
     */
    private String feedback;
    
    /**
     * 企业反馈时间
     */
    private LocalDateTime feedbackTime;
} 