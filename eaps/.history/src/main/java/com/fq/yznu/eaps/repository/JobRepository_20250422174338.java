package com.fq.yznu.eaps.repository;

import com.fq.yznu.eaps.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByStatus(String status, Pageable pageable);

    Page<Job> findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
            String jobTitle, String location, String jobType, Integer salaryMin, Pageable pageable);
}