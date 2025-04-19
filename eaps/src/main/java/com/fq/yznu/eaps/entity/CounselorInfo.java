package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 辅导员信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("counselor_info")
public class CounselorInfo extends BaseEntity {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 辅导员工号
     */
    private String counselorId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
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
     * 负责班级（多个班级以逗号分隔）
     */
    private String responsibleClasses;
} 