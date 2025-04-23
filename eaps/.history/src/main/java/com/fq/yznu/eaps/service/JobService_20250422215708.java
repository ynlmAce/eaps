package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.dao.JobDao;
import com.fq.yznu.eaps.entity.Job;
import com.fq.yznu.eaps.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class JobService {

    @Autowired
    private JobDao jobDao;

    @Transactional
    public Job createJob(@Valid Job job) {
        log.info("Creating new job: {}", job);
        return jobDao.save(job);
    }

    @Transactional(readOnly = true)
    public Job findJobById(Long id) {
        log.info("Finding job by id: {}", id);
        return jobDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    @Transactional
    public Job updateJob(@Valid Job job) {
        log.info("Updating job: {}", job);
        return jobDao.save(job);
    }

    @Transactional
    public void closeJob(Long id) {
        log.info("Closing job with id: {}", id);
        Job job = findJobById(id);
        job.setStatus("closed");
        jobDao.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> searchJobs(String keyword) {
        log.info("Searching jobs with keyword: {}", keyword);
        return jobDao.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }
}