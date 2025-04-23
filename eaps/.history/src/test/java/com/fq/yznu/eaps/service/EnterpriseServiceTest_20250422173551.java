package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.mapper.EnterpriseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise enterprise;
    private Page<Enterprise> page;

    @BeforeEach
    void setUp() {
        enterprise = new Enterprise();
        enterprise.setId(1L);
        enterprise.setEnterpriseName("测试企业");
        enterprise.setType(1);
        enterprise.setScale("中型");
        enterprise.setStatus("0");

        page = new Page<>(1, 10);
    }

    @Test
    void whenCreate_thenReturnEnterprise() {
        when(enterpriseMapper.insert(any(Enterprise.class))).thenReturn(1);
        Enterprise savedEnterprise = enterpriseService.create(enterprise);
        assertThat(savedEnterprise).isNotNull();
        assertThat(savedEnterprise.getEnterpriseName()).isEqualTo("测试企业");
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        IPage<Enterprise> enterprisePage = new Page<>();
        enterprisePage.setRecords(java.util.Collections.singletonList(enterprise));
        when(enterpriseMapper.selectPage(any(), any())).thenReturn(enterprisePage);

        IPage<Enterprise> result = enterpriseService.findByStatus(page, "0");
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("0");
    }

    @Test
    void whenUpdate_thenReturnUpdatedEnterprise() {
        when(enterpriseMapper.updateById(any(Enterprise.class))).thenReturn(1);
        Enterprise updatedEnterprise = enterpriseService.update(enterprise);
        assertThat(updatedEnterprise).isNotNull();
        assertThat(updatedEnterprise.getEnterpriseName()).isEqualTo("测试企业");
    }

    @Test
    void whenApprove_thenReturnApprovedEnterprise() {
        enterprise.setStatus("1");
        when(enterpriseMapper.updateById(any(Enterprise.class))).thenReturn(1);
        Enterprise approvedEnterprise = enterpriseService.approve(1L);
        assertThat(approvedEnterprise).isNotNull();
        assertThat(approvedEnterprise.getStatus()).isEqualTo("1");
    }

    @Test
    void whenReject_thenReturnRejectedEnterprise() {
        enterprise.setStatus("2");
        when(enterpriseMapper.updateById(any(Enterprise.class))).thenReturn(1);
        Enterprise rejectedEnterprise = enterpriseService.reject(1L);
        assertThat(rejectedEnterprise).isNotNull();
        assertThat(rejectedEnterprise.getStatus()).isEqualTo("2");
    }
}