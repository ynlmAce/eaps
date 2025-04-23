package com.fq.yznu.eaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import com.fq.yznu.eaps.repository.UserRepository;

import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private Enterprise enterprise;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        enterprise = new Enterprise();
        enterprise.setId(1L);
        enterprise.setName("测试企业");
        enterprise.setCreditCode("91110000123456789X");
        enterprise.setType("科技企业");
        enterprise.setStatus("pending");
        enterprise.setAddress("北京市海淀区");
        enterprise.setScale("100-499人");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void whenCreateEnterprise_thenReturnSavedEnterprise() {
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        Enterprise savedEnterprise = enterpriseService.create(enterprise);

        assertThat(savedEnterprise).isNotNull();
        assertThat(savedEnterprise.getName()).isEqualTo("测试企业");
        assertThat(savedEnterprise.getStatus()).isEqualTo("pending");
    }

    @Test
    void whenFindByStatus_thenReturnEnterprisePage() {
        Page<Enterprise> page = new PageImpl<>(Arrays.asList(enterprise));
        when(enterpriseRepository.findByStatus("pending", pageable)).thenReturn(page);

        Page<Enterprise> result = enterpriseService.findByStatus("pending", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("测试企业");
    }

    @Test
    void whenUpdateEnterprise_thenReturnUpdatedEnterprise() {
        enterprise.setName("更新后的企业名称");
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        Enterprise updatedEnterprise = enterpriseService.update(enterprise);

        assertThat(updatedEnterprise).isNotNull();
        assertThat(updatedEnterprise.getName()).isEqualTo("更新后的企业名称");
    }

    @Test
    void whenApproveEnterprise_thenStatusShouldBeApproved() {
        when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(enterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        enterpriseService.approve(1L);

        assertThat(enterprise.getStatus()).isEqualTo("approved");
    }

    @Test
    void whenRejectEnterprise_thenStatusShouldBeRejected() {
        when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(enterprise));
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        enterpriseService.reject(1L, "不符合要求");

        assertThat(enterprise.getStatus()).isEqualTo("rejected");
    }
}