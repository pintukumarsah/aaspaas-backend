package com.aaspaas.aaspaas_backend.delivery.websocket;

import com.aaspaas.aaspaas_backend.common.exception.ResourceNotFoundException;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryWebSocketAuthorizationService {

    private final DeliveryAssignmentRepository assignmentRepository;

    public DeliveryWebSocketAuthorizationService(
            DeliveryAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public boolean canAccessDelivery(
            Long assignmentId,
            Long userId
    ) {

        DeliveryAssignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery assignment not found"
                                )
                        );

        Long customerId =
                assignment
                        .getDeliveryRequest()
                        .getCustomer()
                        .getId();

        Long partnerId =
                assignment
                        .getPartner()
                        .getId();

        return userId.equals(customerId)
                || userId.equals(partnerId);
    }
}