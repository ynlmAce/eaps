package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.entity.Job;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import com.fq.yznu.eaps.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @InjectMocks
    private JobService jobService;

    private Job testJob;
    private Enterprise testEnterprise;

    @BeforeEach
    void setUp() {
        testEnterprise = new Enterprise();
        testEnterprise.setId(1L);
        testEnterprise.setEnterpriseName("测试企业");
        testEnterprise.setStatus("approved");

        testJob = new Job();
        testJob.setId(1L);
        testJob.setEnterpriseId(testEnterprise.getId());
        testJob.setJobTitle("Java开发工程师");
        testJob.setDepartment("技术部");
        testJob.setSalaryMin(8000);
        testJob.setSalaryMax(15000);
        testJob.setLocation("昆明市");
        testJob.setJobType("full_time");
        testJob.setStatus("open");
    }

    @Test
    void whenCreateJob_thenReturnSavedJob() {
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        Job savedJob = jobService.create(testJob);

        assertNotNull(savedJob);
        assertEquals(testJob.getJobTitle(), savedJob.getJobTitle());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void whenFindByStatus_thenReturnJobList() {
        Page<Job> jobPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByStatus("open", PageRequest.of(0, 10)))
                .thenReturn(jobPage);

        Page<Job> found = jobService.findByStatus("open", PageRequest.of(0, 10));

        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testJob.getJobTitle(), found.getContent().get(0).getJobTitle());
        verify(jobRepository).findByStatus("open", PageRequest.of(0, 10));
    }

    @Test
    void whenUpdateJob_thenReturnUpdatedJob() {
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        testJob.setSalaryMax(18000);
        Job updatedJob = jobService.update(testJob);

        assertNotNull(updatedJob);
        assertEquals(18000, updatedJob.getSalaryMax());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void whenCloseJob_thenVerifyStatusChange() {
        when(jobRepository.findById(testJob.getId())).thenReturn(java.util.Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        jobService.close(testJob.getId());

        verify(jobRepository).save(any(Job.class));
        assertEquals("closed", testJob.getStatus());
    }

    @Test
    void whenSearchJobs_thenReturnMatchingJobs() {
        Page<Job> jobPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                "Java", "昆明市", "full_time", 7000, PageRequest.of(0, 10)))
                .thenReturn(jobPage);

        Page<Job> found = jobService.search("Java", "昆明市", "full_time", 7000, PageRequest.of(0, 10));

        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testJob.getJobTitle(), found.getContent().get(0).getJobTitle());
        verify(jobRepository).findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                "Java", "昆明市", "full_time", 7000, PageRequest.of(0, 10));
    }
}