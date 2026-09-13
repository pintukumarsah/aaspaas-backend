package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.*;
import com.aaspaas.aaspaas_backend.delivery.entity.*;
import com.aaspaas.aaspaas_backend.delivery.repository.*;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryTrackingService;
@Service
@RequiredArgsConstructor
public class DeliveryTrackingServiceImpl
        implements DeliveryTrackingService {

    private final DeliveryTrackingRepository trackingRepository;

    private final DeliveryStatusHistoryRepository
            statusHistoryRepository;

    private final DeliveryAssignmentRepository
            assignmentRepository;

    private final DeliveryPartnerRepository
            partnerRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public DeliveryTrackingResponse addLocation(
            Long assignmentId,
            CreateDeliveryTrackingRequest request) {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        DeliveryPartner partner =
                partnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                ));

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                ));

        /*
         * Only assigned partner can send GPS.
         */
        if (!assignment.getPartner()
                .getId()
                .equals(partner.getId())) {

            throw new RuntimeException(
                    "You are not assigned to this delivery"
            );
        }

        /*
         * Location only allowed while delivery
         * is active.
         */
        if (!"ACCEPTED".equals(assignment.getStatus())
                && !"PICKED_UP".equals(assignment.getStatus())
                && !"OUT_FOR_DELIVERY".equals(
                        assignment.getStatus())) {

            throw new RuntimeException(
                    "Tracking is not active for this delivery"
            );
        }

        validateCoordinates(
                request.getLatitude(),
                request.getLongitude()
        );

        DeliveryTracking tracking =
                DeliveryTracking.builder()
                        .deliveryAssignment(assignment)
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .accuracyMeters(
                                request.getAccuracyMeters()
                        )
                        .recordedAt(
                                OffsetDateTime.now()
                        )
                        .build();

        tracking =
                trackingRepository.save(tracking);

        return mapTracking(tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getLatestLocation(
            Long assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        verifyCustomerOrPartner(assignment);

        DeliveryTracking tracking =
                trackingRepository
                        .findTopByDeliveryAssignmentIdOrderByRecordedAtDesc(
                                assignmentId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No tracking location found"
                                ));

        return mapTracking(tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryTrackingResponse>
    getTrackingHistory(Long assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        verifyCustomerOrPartner(assignment);

        return trackingRepository
                .findByDeliveryAssignmentIdOrderByRecordedAtAsc(
                        assignmentId
                )
                .stream()
                .map(this::mapTracking)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryStatusHistoryResponse updateStatus(
            Long assignmentId,
            UpdateDeliveryStatusRequest request) {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        DeliveryPartner partner =
                partnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                ));

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        if (!assignment.getPartner()
                .getId()
                .equals(partner.getId())) {

            throw new RuntimeException(
                    "You are not assigned to this delivery"
            );
        }

        String currentStatus =
                assignment.getStatus();

        String newStatus =
                request.getStatus();

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        assignment.setStatus(newStatus);

        assignmentRepository.save(assignment);

        User user =
                userRepository
                        .findByPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        DeliveryStatusHistory history =
                DeliveryStatusHistory.builder()
                        .deliveryAssignment(assignment)
                        .status(newStatus)
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .remarks(request.getRemarks())
                        .createdBy(user)
                        .createdAt(
                                OffsetDateTime.now()
                        )
                        .build();

        history =
                statusHistoryRepository.save(history);

        return mapHistory(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryStatusHistoryResponse>
    getStatusHistory(Long assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        verifyCustomerOrPartner(assignment);

        return statusHistoryRepository
                .findByDeliveryAssignmentIdOrderByCreatedAtAsc(
                        assignmentId
                )
                .stream()
                .map(this::mapHistory)
                .toList();
    }

    private DeliveryAssignment getAssignment(
            Long assignmentId) {

        return assignmentRepository
                .findById(assignmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Delivery assignment not found"
                        ));
    }

    private void verifyCustomerOrPartner(
            DeliveryAssignment assignment) {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user =
                userRepository
                        .findByPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        boolean isCustomer =
                assignment
                        .getDeliveryRequest()
                        .getCustomer()
                        .getId()
                        .equals(user.getId());

        boolean isPartner =
                assignment
                        .getPartner()
                        .getUser()
                        .getId()
                        .equals(user.getId());

        if (!isCustomer && !isPartner) {

            throw new RuntimeException(
                    "You are not allowed to view this delivery"
            );
        }
    }

    private void validateCoordinates(
            BigDecimal latitude,
            BigDecimal longitude) {

        if (latitude == null ||
            longitude == null) {

            throw new RuntimeException(
                    "Latitude and longitude are required"
            );
        }

        if (latitude.compareTo(
                BigDecimal.valueOf(-90)) < 0 ||
            latitude.compareTo(
                BigDecimal.valueOf(90)) > 0) {

            throw new RuntimeException(
                    "Invalid latitude"
            );
        }

        if (longitude.compareTo(
                BigDecimal.valueOf(-180)) < 0 ||
            longitude.compareTo(
                BigDecimal.valueOf(180)) > 0) {

            throw new RuntimeException(
                    "Invalid longitude"
            );
        }
    }

    private void validateStatusTransition(
            String current,
            String next) {

        boolean valid =
                ("ACCEPTED".equals(current)
                        && "OUT_FOR_DELIVERY".equals(next))
                ||
                ("PICKED_UP".equals(current)
                        && "OUT_FOR_DELIVERY".equals(next));

        if (!valid) {

            throw new RuntimeException(
                    "Invalid status transition: "
                            + current
                            + " -> "
                            + next
            );
        }
    }

    private DeliveryTrackingResponse mapTracking(
            DeliveryTracking tracking) {

        return DeliveryTrackingResponse.builder()
                .id(tracking.getId())
                .assignmentId(
                        tracking
                                .getDeliveryAssignment()
                                .getId()
                )
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .accuracyMeters(
                        tracking.getAccuracyMeters()
                )
                .recordedAt(
                        tracking.getRecordedAt()
                )
                .build();
    }

    private DeliveryStatusHistoryResponse mapHistory(
            DeliveryStatusHistory history) {

        return DeliveryStatusHistoryResponse.builder()
                .id(history.getId())
                .assignmentId(
                        history
                                .getDeliveryAssignment()
                                .getId()
                )
                .status(history.getStatus())
                .latitude(history.getLatitude())
                .longitude(history.getLongitude())
                .remarks(history.getRemarks())
                .createdBy(
                        history.getCreatedBy() != null
                                ? history.getCreatedBy().getId()
                                : null
                )
                .createdAt(history.getCreatedAt())
                .build();
    }
}