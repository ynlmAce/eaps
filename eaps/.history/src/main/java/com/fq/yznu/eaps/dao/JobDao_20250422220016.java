package com.fq.yznu.eaps.dao;

import com.fq.yznu.eaps.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDao extends JpaRepository<Job, Long> {
    List<Job> findByTitleContainingOrDescriptionContaining(String title, String description);

    Page<Job> findByStatus(String status, PageRequest pageRequest);

    Page<Job> findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
            String jobTitle, String location, String jobType, Integer salaryMin, PageRequest pageRequest);
}