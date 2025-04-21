package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生信息实体类
 */
@Data
@TableName("student_info")
public class StudentInfo implements Serializable {

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
     * 出生日期
     */
    private String birthDate;

    /**
     * 身份证号
     */
    private String idCardNumber;

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
     * 辅导员ID
     */
    private Long counselorId;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 家庭住址
     */
    private String homeAddress;

    /**
     * 民族
     */
    private String nation;

    /**
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 就业状态（0：未就业，1：已就业，2：考研，3：出国）
     */
    private Integer employmentStatus;

    /**
     * 就业单位
     */
    private String employmentCompany;

    /**
     * 就业职位
     */
    private String employmentPosition;

    /**
     * 就业城市
     */
    private String employmentCity;

    /**
     * 就业薪资
     */
    private Integer employmentSalary;

    /**
     * 简历文件路径
     */
    private String resumePath;

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