package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryOtpResponse;

public interface DeliveryAssignmentService {

    DeliveryAssignmentResponse acceptQuote(Long quoteId);

    DeliveryAssignmentResponse acceptAssignment(
            Long assignmentId
    );

    DeliveryOtpResponse generatePickupOtp(
            Long assignmentId
    );

    DeliveryAssignmentResponse verifyPickupOtp(
            Long assignmentId,
            String otp
    );

    DeliveryOtpResponse generateDeliveryOtp(
            Long assignmentId
    );

    DeliveryAssignmentResponse verifyDeliveryOtp(
            Long assignmentId,
            String otp
    );

    DeliveryAssignmentResponse getMyAssignment();

    DeliveryAssignmentResponse getAssignment(
            Long assignmentId
    );
}