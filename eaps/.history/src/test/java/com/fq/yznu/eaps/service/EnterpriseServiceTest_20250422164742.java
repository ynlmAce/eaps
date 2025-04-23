package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.BaseTest;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import com.fq.yznu.eaps.repository.UserRepository;
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

class EnterpriseServiceTest extends BaseTest {

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise testEnterprise;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("enterprise1");
        testUser.setRole("enterprise");

        testEnterprise = new Enterprise();
        testEnterprise.setId(1L);
        testEnterprise.setUserId(testUser.getId());
        testEnterprise.setEnterpriseName("测试企业");
        testEnterprise.setCreditCode("91530000123456789X");
        testEnterprise.setIndustry("互联网/IT");
        testEnterprise.setScale("medium");
        testEnterprise.setStatus("approved");
    }

    @Test
    void whenCreateEnterprise_thenReturnSavedEnterprise() {
        // 准备测试数据
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(testEnterprise);

        // 执行测试
        Enterprise savedEnterprise = enterpriseService.create(testEnterprise);

        // 验证结果
        assertNotNull(savedEnterprise);
        assertEquals(testEnterprise.getEnterpriseName(), savedEnterprise.getEnterpriseName());
        verify(enterpriseRepository).save(any(Enterprise.class));
    }

    @Test
    void whenFindByStatus_thenReturnEnterpriseList() {
        // 准备测试数据
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Enterprise> enterprisePage = new PageImpl<>(Arrays.asList(testEnterprise));
        when(enterpriseRepository.findByStatus(eq("approved"), any(PageRequest.class)))
                .thenReturn(enterprisePage);

        // 执行测试
        Page<Enterprise> found = enterpriseService.findByStatus("approved", pageRequest);

        // 验证结果
        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testEnterprise.getEnterpriseName(), found.getContent().get(0).getEnterpriseName());
        verify(enterpriseRepository).findByStatus(eq("approved"), any(PageRequest.class));
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        // 准备测试数据
        when(enterpriseRepository.findById(testEnterprise.getId()))
                .thenReturn(Optional.of(testEnterprise));
        when(enterpriseRepository.save(any(Enterprise.class)))
                .thenReturn(testEnterprise);

        // 修改企业信息
        testEnterprise.setEnterpriseName("新企业名称");

        // 执行测试
        Enterprise updatedEnterprise = enterpriseService.update(testEnterprise);

        // 验证结果
        assertNotNull(updatedEnterprise);
        assertEquals("新企业名称", updatedEnterprise.getEnterpriseName());
        verify(enterpriseRepository).save(any(Enterprise.class));
    }

    @Test
    void whenApproveEnterprise_thenVerifyStatusChange() {
        // 准备测试数据
        when(enterpriseRepository.findById(testEnterprise.getId()))
                .thenReturn(Optional.of(testEnterprise));
        when(enterpriseRepository.save(any(Enterprise.class)))
                .thenReturn(testEnterprise);

        // 执行测试
        enterpriseService.approve(testEnterprise.getId());

        // 验证结果
        verify(enterpriseRepository).save(any(Enterprise.class));
        assertEquals("approved", testEnterprise.getStatus());
    }

    @Test
    void whenRejectEnterprise_thenVerifyStatusChange() {
        // 准备测试数据
        String reason = "资质不符";
        when(enterpriseRepository.findById(testEnterprise.getId()))
                .thenReturn(Optional.of(testEnterprise));
        when(enterpriseRepository.save(any(Enterprise.class)))
                .thenReturn(testEnterprise);

        // 执行测试
        enterpriseService.reject(testEnterprise.getId(), reason);

        // 验证结果
        verify(enterpriseRepository).save(any(Enterprise.class));
        assertEquals("rejected", testEnterprise.getStatus());
    }
}