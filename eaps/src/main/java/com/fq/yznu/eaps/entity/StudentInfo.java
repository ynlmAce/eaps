package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_info")
public class StudentInfo extends BaseEntity {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学号
     */
    private String studentId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 学院
     */
    private String college;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 班级
     */
    private String className;
    
    /**
     * 入学年份
     */
    private String enrollmentYear;
    
    /**
     * 毕业年份
     */
    private String graduationYear;
    
    /**
     * 学历层次（0本科 1硕士 2博士）
     */
    private Integer educationLevel;
    
    /**
     * 是否就业（0未就业 1已就业）
     */
    private Integer employmentStatus;
    
    /**
     * 辅导员ID
     */
    private Long counselorId;
    
    /**
     * 个人简介
     */
    private String personalProfile;
    
    /**
     * 简历文件路径
     */
    private String resumePath;
} 