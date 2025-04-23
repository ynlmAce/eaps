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
import java.util.List;

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
        testEnterprise.setLegalPerson("张三");
        testEnterprise.setAddress("测试地址");
        testEnterprise.setIndustry("IT");
        testEnterprise.setScale("中型");
        testEnterprise.setType(2); // 民企
    }

    @Test
    void whenSaveEnterprise_thenReturnSavedEnterprise() {
        when(enterpriseMapper.insert(any(Enterprise.class))).thenReturn(1);

        Enterprise savedEnterprise = enterpriseService.save(testEnterprise);

        assertNotNull(savedEnterprise);
        assertEquals("测试企业", savedEnterprise.getEnterpriseName());
        verify(enterpriseMapper).insert(any(Enterprise.class));
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        Page<Enterprise> page = new Page<>(1, 10);
        List<Enterprise> enterprises = Arrays.asList(testEnterprise);
        page.setRecords(enterprises);
        page.setTotal(1);

        when(enterpriseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Enterprise> result = enterpriseService.page(page,
                new LambdaQueryWrapper<Enterprise>().eq(Enterprise::getStatus, "approved"));

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("测试企业", result.getRecords().get(0).getEnterpriseName());
        verify(enterpriseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        testEnterprise.setEnterpriseName("更新后的企业名称");
        when(enterpriseMapper.updateById(any(Enterprise.class))).thenReturn(1);

        boolean result = enterpriseService.updateById(testEnterprise);

        assertTrue(result);
        verify(enterpriseMapper).updateById(any(Enterprise.class));
    }

    @Test
    void whenDeleteEnterprise_thenReturnTrue() {
        when(enterpriseMapper.deleteById(anyLong())).thenReturn(1);

        boolean result = enterpriseService.removeById(1L);

        assertTrue(result);
        verify(enterpriseMapper).deleteById(anyLong());
    }
}