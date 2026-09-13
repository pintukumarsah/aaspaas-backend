package com.aaspaas.aaspaas_backend.delivery.repository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryOtpRepository
        extends JpaRepository<DeliveryOtp, Long> {

    Optional<DeliveryOtp>
    findTopByDeliveryAssignmentIdAndOtpTypeAndVerifiedAtIsNullOrderByCreatedAtDesc(
            Long deliveryAssignmentId,
            String otpType
    );
}