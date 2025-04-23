package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.mapper.EnterpriseMapper;
import com.fq.yznu.eaps.service.impl.EnterpriseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @InjectMocks
    private EnterpriseServiceImpl enterpriseService;

    private Enterprise testEnterprise;

    @BeforeEach
    void setUp() {
        testEnterprise = new Enterprise();
        testEnterprise.setId(1L);
        testEnterprise.setEnterpriseName("测试企业");
        testEnterprise.setIndustry("IT");
        testEnterprise.setScale("100-499");
        testEnterprise.setLocation("昆明市");
        testEnterprise.setStatus("pending");
    }

    @Test
    void whenCreateEnterprise_thenReturnSavedEnterprise() {
        when(enterpriseMapper.insert(any(Enterprise.class)))
                .thenReturn(1);

        Enterprise savedEnterprise = enterpriseService.create(testEnterprise);

        assertNotNull(savedEnterprise);
        assertEquals(testEnterprise.getEnterpriseName(), savedEnterprise.getEnterpriseName());
        verify(enterpriseMapper).insert(any(Enterprise.class));
    }

    @Test
    void whenFindByStatus_thenReturnEnterpriseList() {
        when(enterpriseMapper.selectList(any()))
                .thenReturn(Arrays.asList(testEnterprise));

        Page<Enterprise> found = enterpriseService.findByStatus("pending", PageRequest.of(0, 10));

        assertNotNull(found);
        assertEquals(1, found.getTotalElements());
        assertEquals(testEnterprise.getEnterpriseName(), found.getContent().get(0).getEnterpriseName());
        verify(enterpriseMapper).selectList(any());
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        when(enterpriseMapper.selectById(testEnterprise.getId()))
                .thenReturn(testEnterprise);
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        testEnterprise.setScale("500-999");
        Enterprise updatedEnterprise = enterpriseService.update(testEnterprise);

        assertNotNull(updatedEnterprise);
        assertEquals("500-999", updatedEnterprise.getScale());
        verify(enterpriseMapper).updateById(any(Enterprise.class));
    }

    @Test
    void whenApproveEnterprise_thenVerifyStatusChange() {
        when(enterpriseMapper.selectById(testEnterprise.getId()))
                .thenReturn(testEnterprise);
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        enterpriseService.approve(testEnterprise.getId());

        verify(enterpriseMapper).updateById(any(Enterprise.class));
        assertEquals("approved", testEnterprise.getStatus());
    }

    @Test
    void whenRejectEnterprise_thenVerifyStatusChange() {
        when(enterpriseMapper.selectById(testEnterprise.getId()))
                .thenReturn(testEnterprise);
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        enterpriseService.reject(testEnterprise.getId());

        verify(enterpriseMapper).updateById(any(Enterprise.class));
        assertEquals("rejected", testEnterprise.getStatus());
    }
}