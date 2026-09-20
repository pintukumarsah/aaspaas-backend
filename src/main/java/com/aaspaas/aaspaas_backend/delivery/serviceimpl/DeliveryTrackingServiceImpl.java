package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.*;
import com.aaspaas.aaspaas_backend.delivery.entity.*;
import com.aaspaas.aaspaas_backend.delivery.repository.*;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryTrackingService;
import com.aaspaas.aaspaas_backend.delivery.websocket.LiveLocationMessage;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private final DeliveryTrackingRepository trackingRepository;
    private final DeliveryStatusHistoryRepository statusHistoryRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DeliveryTrackingServiceImpl(
            DeliveryTrackingRepository trackingRepository,
            DeliveryStatusHistoryRepository statusHistoryRepository,
            DeliveryAssignmentRepository assignmentRepository,
            DeliveryPartnerRepository partnerRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.trackingRepository = trackingRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentRepository = assignmentRepository;
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // =========================================================
    // 1. ADD LOCATION (simple, no broadcast)
    // =========================================================
    @Override
    @Transactional
    public DeliveryTrackingResponse addLocation(
            Long assignmentId,
            CreateDeliveryTrackingRequest request) {

        DeliveryPartner partner = getCurrentPartner();
        DeliveryAssignment assignment = getAssignment(assignmentId);

        if (!assignment.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }

        validateTrackingStatus(assignment);
        validateCoordinates(request.getLatitude(), request.getLongitude());

        DeliveryTracking tracking = DeliveryTracking.builder()
                .deliveryAssignment(assignment)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracyMeters(request.getAccuracyMeters())
                .recordedAt(OffsetDateTime.now())
                .build();

        tracking = trackingRepository.save(tracking);

        return mapTracking(tracking);
    }

    // =========================================================
    // 2. GET LATEST LOCATION
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getLatestLocation(Long assignmentId) {

        DeliveryAssignment assignment = getAssignment(assignmentId);
        verifyCustomerOrPartner(assignment);

        DeliveryTracking tracking = trackingRepository
                .findTopByDeliveryAssignmentIdOrderByRecordedAtDesc(assignmentId)
                .orElseThrow(() -> new RuntimeException("No tracking location found"));

        return mapTracking(tracking);
    }

    // =========================================================
    // 3. GET TRACKING HISTORY
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTrackingResponse> getTrackingHistory(Long assignmentId) {

        DeliveryAssignment assignment = getAssignment(assignmentId);
        verifyCustomerOrPartner(assignment);

        return trackingRepository
                .findByDeliveryAssignmentIdOrderByRecordedAtAsc(assignmentId)
                .stream()
                .map(this::mapTracking)
                .toList();
    }

    // =========================================================
    // 4. UPDATE STATUS (manual status change with history)
    // =========================================================
    @Override
    @Transactional
    public DeliveryStatusHistoryResponse updateStatus(
            Long assignmentId,
            UpdateDeliveryStatusRequest request) {

        User user = getCurrentUser();
        DeliveryPartner partner = getCurrentPartner();
        DeliveryAssignment assignment = getAssignment(assignmentId);

        if (!assignment.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }

        DeliveryAssignmentStatus currentStatus = assignment.getStatus();
        DeliveryAssignmentStatus newStatus;

        try {
            newStatus = DeliveryAssignmentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status value");
        }

        validateStatusTransition(currentStatus, newStatus);

        assignment.setStatus(newStatus);
        assignmentRepository.save(assignment);

        DeliveryStatusHistory history = DeliveryStatusHistory.builder()
                .deliveryAssignment(assignment)
                .status(newStatus)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .remarks(request.getRemarks())
                .createdBy(user.getId())
                .build();

        history = statusHistoryRepository.save(history);

        return mapHistory(history);
    }

    // =========================================================
    // 5. GET STATUS HISTORY
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryStatusHistoryResponse> getStatusHistory(Long assignmentId) {

        DeliveryAssignment assignment = getAssignment(assignmentId);
        verifyCustomerOrPartner(assignment);

        return statusHistoryRepository
                .findByDeliveryAssignmentIdOrderByCreatedAtAsc(assignmentId)
                .stream()
                .map(this::mapHistory)
                .toList();
    }

    // =========================================================
    // 6. UPDATE LOCATION (live tracking, broadcasts over WebSocket)
    // =========================================================
    @Override
    @Transactional
    public DeliveryTrackingResponse updateLocation(
            Long assignmentId,
            LocationUpdateRequest request) {

        DeliveryPartner partner = getCurrentPartner();
        DeliveryAssignment assignment = getAssignment(assignmentId);

        if (!assignment.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }

        validateTrackingStatus(assignment);
        validateCoordinates(request.getLatitude(), request.getLongitude());

        DeliveryTracking tracking = DeliveryTracking.builder()
                .deliveryAssignment(assignment)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracyMeters(request.getAccuracyMeters())
                .recordedAt(OffsetDateTime.now())
                .build();

        DeliveryTracking saved = trackingRepository.save(tracking);

        LiveLocationMessage message = new LiveLocationMessage(
                assignmentId,
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getAccuracyMeters(),
                assignment.getStatus().name(),
                saved.getRecordedAt()
        );

        messagingTemplate.convertAndSend("/topic/delivery/" + assignmentId, message);

        return mapTracking(saved);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void validateTrackingStatus(DeliveryAssignment assignment) {

        DeliveryAssignmentStatus status = assignment.getStatus();

        boolean allowed =
                status == DeliveryAssignmentStatus.ACCEPTED ||
                status == DeliveryAssignmentStatus.PICKUP_OTP_SENT ||
                status == DeliveryAssignmentStatus.PICKED_UP ||
                status == DeliveryAssignmentStatus.OUT_FOR_DELIVERY ||
                status == DeliveryAssignmentStatus.DELIVERY_OTP_SENT;

        if (!allowed) {
            throw new RuntimeException(
                    "Live tracking is not allowed in current status: " + status
            );
        }
    }

    private void validateStatusTransition(
            DeliveryAssignmentStatus current,
            DeliveryAssignmentStatus next) {

        boolean valid =
                (current == DeliveryAssignmentStatus.ACCEPTED && next == DeliveryAssignmentStatus.OUT_FOR_DELIVERY) ||
                (current == DeliveryAssignmentStatus.PICKED_UP && next == DeliveryAssignmentStatus.OUT_FOR_DELIVERY);

        if (!valid) {
            throw new RuntimeException(
                    "Invalid status transition: " + current + " -> " + next
            );
        }
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {

        if (latitude == null || longitude == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
            latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new RuntimeException("Invalid latitude");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
            longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new RuntimeException("Invalid longitude");
        }
    }

    private DeliveryAssignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Delivery assignment not found"));
    }

    private void verifyCustomerOrPartner(DeliveryAssignment assignment) {

        User user = getCurrentUser();

        boolean isCustomer = assignment.getDeliveryRequest()
                .getCustomer().getId().equals(user.getId());

        boolean isPartner = assignment.getPartner()
                .getUser().getId().equals(user.getId());

        if (!isCustomer && !isPartner) {
            throw new RuntimeException("You are not allowed to view this delivery");
        }
    }

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User is not authenticated");
        }

        return userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private DeliveryPartner getCurrentPartner() {

        String phone = SecurityContextHolder.getContext().getAuthentication().getName();

        return partnerRepository.findByUserPhone(phone)
                .orElseThrow(() -> new RuntimeException("Delivery partner profile not found"));
    }

    private DeliveryTrackingResponse mapTracking(DeliveryTracking tracking) {

        return DeliveryTrackingResponse.builder()
                .id(tracking.getId())
                .assignmentId(tracking.getDeliveryAssignment().getId())
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .accuracyMeters(tracking.getAccuracyMeters())
                .recordedAt(tracking.getRecordedAt())
                .build();
    }

    private DeliveryStatusHistoryResponse mapHistory(DeliveryStatusHistory history) {

        return DeliveryStatusHistoryResponse.builder()
                .id(history.getId())
                .assignmentId(history.getDeliveryAssignment().getId())
                .status(history.getStatus().name())
                .latitude(history.getLatitude())
                .longitude(history.getLongitude())
                .remarks(history.getRemarks())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}