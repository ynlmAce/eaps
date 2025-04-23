package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long enterpriseId;

    private String jobTitle;

    private String department;

    private String description;

    private String requirements;

    private Integer salaryMin;

    private Integer salaryMax;

    private String location;

    private String jobType;

    private String educationRequirement;

    private String experienceRequirement;

    private LocalDateTime publishTime;

    private LocalDateTime deadline;

    private String status;

    private Integer applicantsCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}