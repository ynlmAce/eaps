package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.mapper.EnterpriseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise testEnterprise;

    @BeforeEach
    void setUp() {
        testEnterprise = new Enterprise();
        testEnterprise.setId(1L);
        testEnterprise.setEnterpriseName("测试企业");
        testEnterprise.setLegalPerson("张三");
        testEnterprise.setCreditCode("91110000123456789X");
        testEnterprise.setType(1);
        testEnterprise.setScale("中型");
        testEnterprise.setIndustry("IT");
        testEnterprise.setStatus("pending");
    }

    @Test
    void whenSaveEnterprise_thenReturnSavedEnterprise() {
        when(enterpriseMapper.insert(any(Enterprise.class))).thenReturn(1);
        when(enterpriseMapper.selectById(any(Long.class))).thenReturn(testEnterprise);

        Enterprise savedEnterprise = enterpriseService.save(testEnterprise);

        assertThat(savedEnterprise).isNotNull();
        assertThat(savedEnterprise.getEnterpriseName()).isEqualTo(testEnterprise.getEnterpriseName());
        assertThat(savedEnterprise.getLegalPerson()).isEqualTo(testEnterprise.getLegalPerson());
        verify(enterpriseMapper).insert(any(Enterprise.class));
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        Page<Enterprise> page = new Page<>(1, 10);
        List<Enterprise> enterprises = Arrays.asList(testEnterprise);
        page.setRecords(enterprises);
        page.setTotal(1);

        when(enterpriseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Enterprise> result = enterpriseService.findByStatus("pending", page);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("pending");
        verify(enterpriseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        when(enterpriseMapper.updateById(any(Enterprise.class))).thenReturn(1);
        when(enterpriseMapper.selectById(any(Long.class))).thenReturn(testEnterprise);

        testEnterprise.setEnterpriseName("更新后的企业名称");
        Enterprise updatedEnterprise = enterpriseService.updateById(testEnterprise);

        assertThat(updatedEnterprise).isNotNull();
        assertThat(updatedEnterprise.getEnterpriseName()).isEqualTo("更新后的企业名称");
        verify(enterpriseMapper).updateById(any(Enterprise.class));
    }

    @Test
    void whenDeleteEnterprise_thenReturnTrue() {
        when(enterpriseMapper.deleteById(any(Long.class))).thenReturn(1);

        boolean result = enterpriseService.removeById(testEnterprise.getId());

        assertThat(result).isTrue();
        verify(enterpriseMapper).deleteById(testEnterprise.getId());
    }
}