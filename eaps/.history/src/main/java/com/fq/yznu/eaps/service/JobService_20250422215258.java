package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Job;
import com.fq.yznu.eaps.repository.JobRepository;
import com.fq.yznu.eaps.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Transactional
    public Job create(@Valid Job job) {
        log.info("Creating new job position: {}", job.getJobTitle());
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Page<Job> findByStatus(String status, Pageable pageable) {
        log.debug("Finding jobs by status: {}", status);
        return jobRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Job update(@Valid Job job) {
        log.info("Updating job position: {}", job.getId());
        if (!jobRepository.existsById(job.getId())) {
            throw new ResourceNotFoundException("Job not found with id: " + job.getId());
        }
        return jobRepository.save(job);
    }

    @Transactional
    public void close(Long id) {
        log.info("Closing job position with id: {}", id);
        jobRepository.findById(id)
                .map(job -> {
                    job.setStatus("closed");
                    return jobRepository.save(job);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Job> search(String jobTitle, String location, String jobType, Integer salaryMin, Pageable pageable) {
        log.debug("Searching jobs with criteria - title: {}, location: {}, type: {}, minSalary: {}",
                jobTitle, location, jobType, salaryMin);
        return jobRepository.findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                jobTitle, location, jobType, salaryMin, pageable);
    }
}