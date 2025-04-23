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
        return jobRepository.insert(job) > 0 ? job : null;
    }

    public Page<Job> findByStatus(String status, Pageable pageable) {
        return jobRepository.findByStatus(status, pageable);
    }

    public Job update(Job job) {
        return jobRepository.updateById(job) > 0 ? job : null;
    }

    public void close(Long id) {
        Job job = jobRepository.selectById(id);
        if (job != null) {
            job.setStatus("closed");
            jobRepository.updateById(job);
        }
    }

    public Page<Job> search(String jobTitle, String location, String jobType, Integer salaryMin, Pageable pageable) {
        return jobRepository.findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                jobTitle, location, jobType, salaryMin, pageable);
    }
}