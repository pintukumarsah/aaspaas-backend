package com.aaspaas.aaspaas_backend.business.repository;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository
        extends JpaRepository<Business, Long> {

    List<Business> findByOwnerId(Long ownerId);

    List<Business> findByStatus(String status);

    Page<Business> findByStatus(
            String status,
            Pageable pageable
    );
}