package com.aaspaas.aaspaas_backend.delivery.service;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryStatusHistoryResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.OtpResponse;

import java.util.List;

public interface DeliveryAssignmentService {

    DeliveryAssignmentResponse acceptQuote(Long quoteId);

    DeliveryAssignmentResponse acceptAssignment(Long assignmentId);

    DeliveryAssignmentResponse rejectAssignment(Long assignmentId);

    DeliveryAssignmentResponse cancelAssignment(Long assignmentId);

    OtpResponse generatePickupOtp(Long assignmentId);

    DeliveryAssignmentResponse verifyPickupOtp(Long assignmentId, String otp);

    DeliveryAssignmentResponse startDelivery(Long assignmentId);

    OtpResponse generateDeliveryOtp(Long assignmentId);

    DeliveryAssignmentResponse verifyDeliveryOtp(Long assignmentId, String otp);

    DeliveryAssignmentResponse getMyAssignment();

    DeliveryAssignmentResponse getAssignment(Long assignmentId);

    List<DeliveryStatusHistoryResponse> getHistory(Long assignmentId);
}