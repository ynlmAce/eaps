package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 辅导员信息实体类
 */
@Data
@TableName("counselor_info")
public class CounselorInfo implements Serializable {

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
     * 工号
     */
    private String workNumber;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 性别（0：男，1：女）
     */
    private Integer gender;
    
    /**
     * 所属学校
     */
    private String university;
    
    /**
     * 所属学院
     */
    private String college;
    
    /**
     * 职称
     */
    private String title;
    
    /**
     * 办公室地址
     */
    private String officeAddress;
    
    /**
     * 办公室电话
     */
    private String officePhone;
    
    /**
     * 手机号码
     */
    private String mobile;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 负责专业（逗号分隔）
     */
    private String responsibleMajors;
    
    /**
     * 负责班级（逗号分隔）
     */
    private String responsibleClasses;
    
    /**
     * 管理的学生数量
     */
    private Integer studentCount;
    
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