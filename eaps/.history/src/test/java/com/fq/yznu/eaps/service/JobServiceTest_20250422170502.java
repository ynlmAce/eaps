package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.BaseTest;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.entity.Job;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import com.fq.yznu.eaps.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobServiceTest extends BaseTest {

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
        MockitoAnnotations.openMocks(this);

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
        when(enterpriseRepository.findById(testEnterprise.getId()))
                .thenReturn(Optional.of(testEnterprise));
        when(jobRepository.insert(any(Job.class)))
                .thenReturn(1);

        Job savedJob = jobService.create(testJob);

        assertNotNull(savedJob);
        assertEquals(testJob.getJobTitle(), savedJob.getJobTitle());
        verify(jobRepository).insert(any(Job.class));
    }

    @Test
    void whenFindByStatus_thenReturnJobList() {
        // 准备测试数据
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Job> jobPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByStatus(eq("open"), any(PageRequest.class)))
                .thenReturn(jobPage);

        // 执行测试
        Page<Job> found = jobService.findByStatus("open", pageRequest);

        // 验证结果
        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testJob.getJobTitle(), found.getContent().get(0).getJobTitle());
        verify(jobRepository).findByStatus(eq("open"), any(PageRequest.class));
    }

    @Test
    void whenUpdateJob_thenReturnUpdatedJob() {
        when(jobRepository.selectById(testJob.getId()))
                .thenReturn(testJob);
        when(jobRepository.updateById(any(Job.class)))
                .thenReturn(1);

        testJob.setSalaryMax(18000);
        Job updatedJob = jobService.update(testJob);

        assertNotNull(updatedJob);
        assertEquals(18000, updatedJob.getSalaryMax());
        verify(jobRepository).updateById(any(Job.class));
    }

    @Test
    void whenCloseJob_thenVerifyStatusChange() {
        when(jobRepository.selectById(testJob.getId()))
                .thenReturn(testJob);
        when(jobRepository.updateById(any(Job.class)))
                .thenReturn(1);

        jobService.close(testJob.getId());

        verify(jobRepository).updateById(any(Job.class));
        assertEquals("closed", testJob.getStatus());
    }

    @Test
    void whenSearchJobs_thenReturnMatchingJobs() {
        // 准备测试数据
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Job> jobPage = new PageImpl<>(Arrays.asList(testJob));
        when(jobRepository.findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                eq("Java"), eq("昆明市"), eq("full_time"), eq(7000), any(PageRequest.class)))
                .thenReturn(jobPage);

        // 执行测试
        Page<Job> found = jobService.search("Java", "昆明市", "full_time", 7000, pageRequest);

        // 验证结果
        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testJob.getJobTitle(), found.getContent().get(0).getJobTitle());
        verify(jobRepository).findByJobTitleContainingAndLocationAndJobTypeAndSalaryMinGreaterThanEqual(
                eq("Java"), eq("昆明市"), eq("full_time"), eq(7000), any(PageRequest.class));
    }
}