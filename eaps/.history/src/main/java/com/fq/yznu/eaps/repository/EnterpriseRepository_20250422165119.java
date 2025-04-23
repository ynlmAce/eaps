package com.fq.yznu.eaps.repository;

import com.fq.yznu.eaps.entity.Enterprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Page<Enterprise> findByStatus(String status, Pageable pageable);
}