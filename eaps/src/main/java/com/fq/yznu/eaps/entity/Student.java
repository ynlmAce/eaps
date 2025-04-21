package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生实体类
 */
@Data
@TableName("student")
public class Student implements Serializable {

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
     * 学号
     */
    private String studentNumber;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 性别（0：男，1：女）
     */
    private Integer gender;
    
    /**
     * 学校
     */
    private String university;
    
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
     * 学历（1：专科，2：本科，3：硕士，4：博士）
     */
    private Integer education;
    
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
    
    /**
     * 毕业年份
     */
    private Integer graduationYear;
    
    /**
     * 是否毕业（0：未毕业，1：已毕业）
     */
    private Integer graduated;
    
    /**
     * 就业状态（0：未就业，1：已就业，2：考研，3：出国）
     */
    private Integer employmentStatus;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 家庭住址
     */
    private String homeAddress;
    
    /**
     * 简历路径
     */
    private String resumePath;
    
    /**
     * 辅导员ID
     */
    private Long counselorId;
    
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