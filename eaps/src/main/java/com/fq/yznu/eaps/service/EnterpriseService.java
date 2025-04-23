package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.repository.EnterpriseRepository;
import com.fq.yznu.eaps.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    public Enterprise create(Enterprise enterprise) {
        return enterpriseRepository.save(enterprise);
    }

    public Page<Enterprise> findByStatus(String status, Pageable pageable) {
        return enterpriseRepository.findByStatus(status, pageable);
    }

    public Enterprise update(Enterprise enterprise) {
        return enterpriseRepository.save(enterprise);
    }

    public void approve(Long id) {
        enterpriseRepository.findById(id).ifPresent(enterprise -> {
            enterprise.setStatus("approved");
            enterpriseRepository.save(enterprise);
        });
    }

    public void reject(Long id, String reason) {
        enterpriseRepository.findById(id).ifPresent(enterprise -> {
            enterprise.setStatus("rejected");
            enterpriseRepository.save(enterprise);
        });
    }
}