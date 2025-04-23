package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise enterprise;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        enterprise = new Enterprise();
        enterprise.setId(1L);
        enterprise.setEnterpriseName("测试企业");
        enterprise.setType(1);
        enterprise.setScale("中型");
        enterprise.setStatus("0");
        enterprise.setIndustry("互联网/IT");
        enterprise.setAddress("测试地址");
        enterprise.setPhone("13800138000");
        enterprise.setEmail("test@example.com");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void whenCreate_thenReturnEnterprise() {
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);
        Enterprise savedEnterprise = enterpriseService.create(enterprise);
        assertThat(savedEnterprise).isNotNull();
        assertThat(savedEnterprise.getEnterpriseName()).isEqualTo("测试企业");
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        Page<Enterprise> enterprisePage = new PageImpl<>(Collections.singletonList(enterprise));
        when(enterpriseRepository.findByStatus("0", pageable)).thenReturn(enterprisePage);

        Page<Enterprise> result = enterpriseService.findByStatus("0", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("0");
    }

    @Test
    void whenUpdate_thenReturnUpdatedEnterprise() {
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);
        Enterprise updatedEnterprise = enterpriseService.update(enterprise);
        assertThat(updatedEnterprise).isNotNull();
        assertThat(updatedEnterprise.getEnterpriseName()).isEqualTo("测试企业");
    }

    @Test
    void whenApprove_thenStatusShouldBeApproved() {
        when(enterpriseRepository.findById(1L)).thenReturn(java.util.Optional.of(enterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        enterpriseService.approve(1L);
        assertThat(enterprise.getStatus()).isEqualTo("approved");
    }

    @Test
    void whenReject_thenStatusShouldBeRejected() {
        when(enterpriseRepository.findById(1L)).thenReturn(java.util.Optional.of(enterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        enterpriseService.reject(1L, "拒绝原因");
        assertThat(enterprise.getStatus()).isEqualTo("rejected");
    }
}