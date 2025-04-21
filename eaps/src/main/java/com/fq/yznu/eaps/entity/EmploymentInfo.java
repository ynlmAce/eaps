package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 就业信息实体类
 */
@Data
@TableName("employment_info")
public class EmploymentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /**
     * 学生姓名
     */
    @TableField(exist = false)
    private String studentName;

    /**
     * 就业状态：0未就业，1已就业，2升学，3创业，4灵活就业
     */
    @NotNull(message = "就业状态不能为空")
    private Integer employmentStatus;

    /**
     * 就业类型：1合同就业，2灵活就业
     */
    private Integer employmentType;

    /**
     * 单位名称
     */
    private String companyName;

    /**
     * 单位性质：1国有企业，2民营企业，3外资企业，4政府机构，5事业单位，6其他
     */
    private Integer companyType;

    /**
     * 工作城市
     */
    private String workCity;

    /**
     * 工作省份
     */
    private String workProvince;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 月薪（元）
     */
    private BigDecimal monthlySalary;

    /**
     * 合同期限（月）
     */
    private Integer contractDuration;

    /**
     * 升学院校
     */
    private String furtherSchool;

    /**
     * 升学专业
     */
    private String furtherMajor;

    /**
     * 学历层次：1硕士，2博士，3其他
     */
    private Integer educationLevel;

    /**
     * 创业项目名称
     */
    private String entrepreneurshipProject;

    /**
     * 创业项目描述
     */
    private String entrepreneurshipDescription;

    /**
     * 证明材料URL
     */
    private String documentUrl;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 审核状态：0待审核，1已通过，2已拒绝
     */
    private Integer verifyStatus;

    /**
     * 审核备注
     */
    private String verifyRemark;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyTime;

    /**
     * 毕业年份
     */
    @NotNull(message = "毕业年份不能为空")
    private Integer graduationYear;

    /**
     * 学院ID
     */
    private Long collegeId;

    /**
     * 学院名称（冗余字段，非表字段）
     */
    @TableField(exist = false)
    private String collegeName;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称（冗余字段，非表字段）
     */
    @TableField(exist = false)
    private String majorName;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称（冗余字段，非表字段）
     */
    @TableField(exist = false)
    private String className;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
}