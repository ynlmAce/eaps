package com.fq.yznu.eaps.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String role;
    private String email;
    private String phone;
    private String avatar;
    private String status;

    // 学生特有信息
    private String studentNumber;
    private String realName;
    private String university;
    private String college;
    private String major;
    private String className;
    private String grade;

    // 企业特有信息
    private String enterpriseName;
    private String industry;
    private String scale;
    private String description;
    private String logoPath;

    // 辅导员特有信息
    private String counselorName;
    private String employeeNumber;
    private String counselorCollege;
    private String position;
}