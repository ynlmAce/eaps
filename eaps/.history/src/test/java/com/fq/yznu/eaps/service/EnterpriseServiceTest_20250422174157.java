package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Enterprise;
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
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnterpriseServiceTest {

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise testEnterprise;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEnterprise = new Enterprise();
        testEnterprise.setId(1L);
        testEnterprise.setEnterpriseName("测试企业");
        testEnterprise.setType(1);
        testEnterprise.setScale("中型");
        testEnterprise.setStatus("pending");
        testEnterprise.setIndustry("互联网/IT");
        testEnterprise.setAddress("测试地址");
        testEnterprise.setPhone("13800138000");
        testEnterprise.setEmail("test@example.com");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void whenCreate_thenReturnSavedEnterprise() {
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(testEnterprise);

        Enterprise savedEnterprise = enterpriseService.create(testEnterprise);

        assertNotNull(savedEnterprise);
        assertEquals(testEnterprise.getEnterpriseName(), savedEnterprise.getEnterpriseName());
        verify(enterpriseRepository).save(any(Enterprise.class));
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        Page<Enterprise> enterprisePage = new PageImpl<>(Collections.singletonList(testEnterprise));
        when(enterpriseRepository.findByStatus("pending", pageable)).thenReturn(enterprisePage);

        Page<Enterprise> result = enterpriseService.findByStatus("pending", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("pending", result.getContent().get(0).getStatus());
        verify(enterpriseRepository).findByStatus("pending", pageable);
    }

    @Test
    void whenUpdate_thenReturnUpdatedEnterprise() {
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(testEnterprise);

        Enterprise updatedEnterprise = enterpriseService.update(testEnterprise);

        assertNotNull(updatedEnterprise);
        assertEquals(testEnterprise.getEnterpriseName(), updatedEnterprise.getEnterpriseName());
        verify(enterpriseRepository).save(testEnterprise);
    }

    @Test
    void whenApprove_thenUpdateStatus() {
        when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(testEnterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(testEnterprise);

        enterpriseService.approve(1L);

        verify(enterpriseRepository).findById(1L);
        verify(enterpriseRepository).save(any(Enterprise.class));
        assertEquals("approved", testEnterprise.getStatus());
    }

    @Test
    void whenReject_thenUpdateStatusAndReason() {
        when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(testEnterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(testEnterprise);

        String reason = "资质不符";
        enterpriseService.reject(1L, reason);

        verify(enterpriseRepository).findById(1L);
        verify(enterpriseRepository).save(any(Enterprise.class));
        assertEquals("rejected", testEnterprise.getStatus());
    }
}