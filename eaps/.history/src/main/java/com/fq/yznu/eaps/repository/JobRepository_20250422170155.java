package com.fq.yznu.eaps.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Mapper
public interface JobRepository extends BaseMapper<Job> {
    Page<Job> findByStatus(String status, Pageable pageable);

    Page<Job> findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
            String jobTitle, String location, String jobType, Integer salaryMin, Pageable pageable);
}