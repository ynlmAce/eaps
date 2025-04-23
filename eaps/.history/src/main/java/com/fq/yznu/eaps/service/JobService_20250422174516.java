package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Job;
import com.fq.yznu.eaps.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job create(Job job) {
        return jobRepository.save(job);
    }

    public Page<Job> findByStatus(String status, Pageable pageable) {
        return jobRepository.findByStatus(status, pageable);
    }

    public Job update(Job job) {
        return jobRepository.save(job);
    }

    public void close(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("closed");
            jobRepository.save(job);
        });
    }

    public Page<Job> search(String jobTitle, String location, String jobType, Integer salaryMin, Pageable pageable) {
        return jobRepository.findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                jobTitle, location, jobType, salaryMin, pageable);
    }
}