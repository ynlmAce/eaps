package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.mapper.EnterpriseMapper;
import com.fq.yznu.eaps.service.impl.EnterpriseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        testEnterprise.setScale("中型");
        testEnterprise.setAddress("昆明市");
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
        Page<Enterprise> page = new Page<>(1, 10);
        when(enterpriseMapper.selectPage(any(), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Enterprise>(1, 10).setRecords(Arrays.asList(testEnterprise)));

        Page<Enterprise> found = enterpriseService.findByStatus("pending", page);

        assertNotNull(found);
        assertEquals(1, found.getRecords().size());
        assertEquals(testEnterprise.getEnterpriseName(), found.getRecords().get(0).getEnterpriseName());
        verify(enterpriseMapper).selectPage(any(), any(LambdaQueryWrapper.class));
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        Enterprise updatedEnterprise = enterpriseService.update(testEnterprise);

        assertNotNull(updatedEnterprise);
        assertEquals(testEnterprise.getEnterpriseName(), updatedEnterprise.getEnterpriseName());
        verify(enterpriseMapper).updateById(any(Enterprise.class));
    }

    @Test
    void whenApproveEnterprise_thenStatusShouldBeApproved() {
        when(enterpriseMapper.selectById(anyLong()))
                .thenReturn(testEnterprise);
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        enterpriseService.approve(1L);

        assertEquals("approved", testEnterprise.getStatus());
        verify(enterpriseMapper).updateById(testEnterprise);
    }

    @Test
    void whenRejectEnterprise_thenStatusShouldBeRejected() {
        when(enterpriseMapper.selectById(anyLong()))
                .thenReturn(testEnterprise);
        when(enterpriseMapper.updateById(any(Enterprise.class)))
                .thenReturn(1);

        String reason = "不符合要求";
        enterpriseService.reject(1L, reason);

        assertEquals("rejected", testEnterprise.getStatus());
        verify(enterpriseMapper).updateById(testEnterprise);
    }
}