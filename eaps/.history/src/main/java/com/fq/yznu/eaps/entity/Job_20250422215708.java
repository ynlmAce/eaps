package com.fq.yznu.eaps.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "job_title")
    private String jobTitle;

    private String department;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String requirements;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    private String location;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "education_requirement")
    private String educationRequirement;

    @Column(name = "experience_requirement")
    private String experienceRequirement;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    private LocalDateTime deadline;

    private String status;

    @Column(name = "applicants_count")
    private Integer applicantsCount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}