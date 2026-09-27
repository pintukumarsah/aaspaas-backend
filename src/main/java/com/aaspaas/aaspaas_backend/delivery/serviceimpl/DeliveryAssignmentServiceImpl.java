package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryStatusHistoryResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.OtpResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.*;
import com.aaspaas.aaspaas_backend.delivery.repository.*;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryAssignmentService;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentServiceImpl implements DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryRequestRepository requestRepository;
    private final DeliveryQuoteRepository quoteRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryOtpRepository otpRepository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private long otpExpiryMinutes;

    @Value("${app.otp.development-mode:false}")
    private boolean developmentOtpMode;

    // =========================================================
    // 1. ACCEPT QUOTE
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse acceptQuote(Long quoteId) {

        User customer = getCurrentUser();

        DeliveryQuote quote = quoteRepository.findByIdForUpdate(quoteId)
                .orElseThrow(() -> new RuntimeException("Delivery quote not found"));

        DeliveryRequest deliveryRequest = requestRepository
                .findByIdForUpdate(quote.getDeliveryRequest().getId())
                .orElseThrow(() -> new RuntimeException("Delivery request not found"));

        if (!deliveryRequest.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not allowed to accept this quote");
        }
        if (!"OPEN".equals(deliveryRequest.getStatus())) {
            throw new RuntimeException("Delivery request is no longer open");
        }
        if (quote.getStatus() != com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus.PENDING) {
            throw new RuntimeException("This quote is no longer available");
        }
        if (deliveryRequest.getExpiresAt() != null
                && deliveryRequest.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Delivery request has expired");
        }
        if (assignmentRepository.existsByDeliveryRequestId(deliveryRequest.getId())) {
            throw new RuntimeException("Delivery partner already assigned");
        }

        DeliveryPartner partner = quote.getPartner();

        quote.setStatus(com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus.ACCEPTED);
        quoteRepository.save(quote);

        List<DeliveryQuote> allQuotes = quoteRepository
                .findByDeliveryRequestIdOrderByQuotedAmountAsc(deliveryRequest.getId());
        for (DeliveryQuote other : allQuotes) {
            if (!other.getId().equals(quote.getId()) && other.getStatus() == com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus.PENDING) {
                other.setStatus(com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus.REJECTED);
            }
        }
        quoteRepository.saveAll(allQuotes);

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .deliveryRequest(deliveryRequest)
                .partner(partner)
                .quote(quote)
                .status(DeliveryAssignmentStatus.ASSIGNED)
                .assignedAt(OffsetDateTime.now())
                .build();

        assignment = assignmentRepository.save(assignment);

        deliveryRequest.setStatus("ASSIGNED");
        requestRepository.save(deliveryRequest);

        partner.setAvailabilityStatus(DeliveryPartnerAvailabilityStatus.BUSY);
        partnerRepository.save(partner);

        notificationService.createNotification(
                partner.getUser().getId(),
                "New Delivery Assignment",
                "You have been assigned a new delivery.",
                "DELIVERY_ASSIGNED",
                "DELIVERY_ASSIGNMENT",
                assignment.getId()
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 2. ACCEPT ASSIGNMENT
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse acceptAssignment(Long assignmentId) {

        User partnerUser = getCurrentUser();
        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (!assignment.getPartner().getUser().getId().equals(partnerUser.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }
        if (assignment.getStatus() != DeliveryAssignmentStatus.ASSIGNED) {
            throw new RuntimeException("Assignment cannot be accepted in current status");
        }

        assignment.setStatus(DeliveryAssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(OffsetDateTime.now());
        assignment = assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.ACCEPTED, partnerUser.getId(),
                "Delivery partner accepted assignment");

        notificationService.createNotification(
                assignment.getDeliveryRequest().getCustomer().getId(),
                "Delivery Accepted",
                "Your delivery partner accepted the assignment.",
                "DELIVERY_ACCEPTED",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 3. REJECT ASSIGNMENT
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse rejectAssignment(Long assignmentId) {

        User partnerUser = getCurrentUser();
        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (!assignment.getPartner().getUser().getId().equals(partnerUser.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }
        if (assignment.getStatus() != DeliveryAssignmentStatus.ASSIGNED) {
            throw new RuntimeException("Only ASSIGNED assignment can be rejected");
        }

        assignment.setStatus(DeliveryAssignmentStatus.REJECTED);
        assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.REJECTED, partnerUser.getId(),
                "Delivery partner rejected assignment");

        notificationService.createNotification(
                assignment.getDeliveryRequest().getCustomer().getId(),
                "Delivery Partner Rejected",
                "The assigned delivery partner rejected the delivery.",
                "DELIVERY_REJECTED",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 4. CANCEL ASSIGNMENT
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse cancelAssignment(Long assignmentId) {

        User currentUser = getCurrentUser();
        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        Long customerId = assignment.getDeliveryRequest().getCustomer().getId();
        Long partnerUserId = assignment.getPartner().getUser().getId();

        boolean isCustomer = customerId.equals(currentUser.getId());
        boolean isPartner = partnerUserId.equals(currentUser.getId());

        if (!isCustomer && !isPartner) {
            throw new RuntimeException("You are not allowed to cancel this assignment");
        }
        if (assignment.getStatus() == DeliveryAssignmentStatus.DELIVERED) {
            throw new RuntimeException("Delivered assignment cannot be cancelled");
        }

        assignment.setStatus(DeliveryAssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.CANCELLED, currentUser.getId(),
                "Delivery assignment cancelled");

        Long notifyUserId = isCustomer ? partnerUserId : customerId;

        notificationService.createNotification(
                notifyUserId,
                "Delivery Cancelled",
                "The delivery assignment has been cancelled.",
                "DELIVERY_CANCELLED",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 5. GENERATE PICKUP OTP
    // =========================================================
    @Override
    @Transactional
    public OtpResponse generatePickupOtp(Long assignmentId) {

        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (assignment.getStatus() != DeliveryAssignmentStatus.ACCEPTED) {
            throw new RuntimeException("Partner must accept assignment first");
        }

        return issueOtp(assignment, DeliveryOtpType.PICKUP);
    }

    // =========================================================
    // 6. VERIFY PICKUP OTP
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse verifyPickupOtp(Long assignmentId, String otp) {

        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (assignment.getStatus() != DeliveryAssignmentStatus.ACCEPTED
                && assignment.getStatus() != DeliveryAssignmentStatus.PICKUP_OTP_SENT) {
            throw new RuntimeException("Pickup OTP cannot be verified now");
        }

        DeliveryOtp deliveryOtp = otpRepository
                .findTopByDeliveryAssignmentIdAndOtpTypeAndVerifiedAtIsNullOrderByCreatedAtDesc(
                        assignmentId, DeliveryOtpType.PICKUP)
                .orElseThrow(() -> new RuntimeException("Pickup OTP not found"));

        validateOtp(deliveryOtp, otp);

        deliveryOtp.setVerifiedAt(OffsetDateTime.now());
        otpRepository.save(deliveryOtp);

        assignment.setStatus(DeliveryAssignmentStatus.PICKED_UP);
        assignment.setPickedUpAt(OffsetDateTime.now());
        assignment = assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.PICKED_UP, getCurrentUser().getId(),
                "Pickup OTP verified");

        notificationService.createNotification(
                assignment.getDeliveryRequest().getCustomer().getId(),
                "Item Picked Up",
                "Your item has been picked up by the delivery partner.",
                "PICKED_UP",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 7. START DELIVERY (OUT FOR DELIVERY)
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse startDelivery(Long assignmentId) {

        User partnerUser = getCurrentUser();
        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (!assignment.getPartner().getUser().getId().equals(partnerUser.getId())) {
            throw new RuntimeException("You are not assigned to this delivery");
        }
        if (assignment.getStatus() != DeliveryAssignmentStatus.PICKED_UP) {
            throw new RuntimeException("Delivery can start only after pickup");
        }

        assignment.setStatus(DeliveryAssignmentStatus.OUT_FOR_DELIVERY);
        assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.OUT_FOR_DELIVERY, partnerUser.getId(),
                "Delivery partner started delivery");

        notificationService.createNotification(
                assignment.getDeliveryRequest().getCustomer().getId(),
                "Order Out For Delivery",
                "Your order is now out for delivery.",
                "OUT_FOR_DELIVERY",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 8. GENERATE DELIVERY OTP
    // =========================================================
    @Override
    @Transactional
    public OtpResponse generateDeliveryOtp(Long assignmentId) {

        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (assignment.getStatus() != DeliveryAssignmentStatus.PICKED_UP
                && assignment.getStatus() != DeliveryAssignmentStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Item must be picked up first");
        }

        return issueOtp(assignment, DeliveryOtpType.DELIVERY);
    }

    // =========================================================
    // 9. VERIFY DELIVERY OTP
    // =========================================================
    @Override
    @Transactional
    public DeliveryAssignmentResponse verifyDeliveryOtp(Long assignmentId, String otp) {

        DeliveryAssignment assignment = getAssignmentEntity(assignmentId);

        if (assignment.getStatus() != DeliveryAssignmentStatus.PICKED_UP
                && assignment.getStatus() != DeliveryAssignmentStatus.OUT_FOR_DELIVERY
                && assignment.getStatus() != DeliveryAssignmentStatus.DELIVERY_OTP_SENT) {
            throw new RuntimeException("Delivery OTP cannot be verified now");
        }

        DeliveryOtp deliveryOtp = otpRepository
                .findTopByDeliveryAssignmentIdAndOtpTypeAndVerifiedAtIsNullOrderByCreatedAtDesc(
                        assignmentId, DeliveryOtpType.DELIVERY)
                .orElseThrow(() -> new RuntimeException("Delivery OTP not found"));

        validateOtp(deliveryOtp, otp);

        deliveryOtp.setVerifiedAt(OffsetDateTime.now());
        otpRepository.save(deliveryOtp);

        assignment.setStatus(DeliveryAssignmentStatus.DELIVERED);
        assignment.setDeliveredAt(OffsetDateTime.now());
        assignment = assignmentRepository.save(assignment);

        saveHistory(assignment, DeliveryAssignmentStatus.DELIVERED, getCurrentUser().getId(),
                "Delivery OTP verified");

        DeliveryPartner partner = assignment.getPartner();
        partner.setAvailabilityStatus(DeliveryPartnerAvailabilityStatus.AVAILABLE);
        Integer total = partner.getTotalDeliveries();
        partner.setTotalDeliveries(total == null ? 1 : total + 1);
        partnerRepository.save(partner);

        notificationService.createNotification(
                assignment.getDeliveryRequest().getCustomer().getId(),
                "Order Delivered",
                "Your order has been delivered successfully.",
                "DELIVERED",
                "DELIVERY_ASSIGNMENT",
                assignmentId
        );

        return mapToResponse(assignment);
    }

    // =========================================================
    // 10. GET MY ASSIGNMENT (current delivery partner)
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public DeliveryAssignmentResponse getMyAssignment() {

        User user = getCurrentUser();

        DeliveryPartner partner = partnerRepository.findByUserPhone(user.getPhone())
                .orElseThrow(() -> new RuntimeException("Delivery partner profile not found"));

        DeliveryAssignment assignment = assignmentRepository
                .findFirstByPartnerIdAndStatusNotOrderByAssignedAtDesc(
                        partner.getId(), DeliveryAssignmentStatus.DELIVERED)
                .orElseThrow(() -> new RuntimeException("No active delivery assignment found"));

        return mapToResponse(assignment);
    }

    // =========================================================
    // 11. GET ASSIGNMENT BY ID
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public DeliveryAssignmentResponse getAssignment(Long assignmentId) {
        return mapToResponse(getAssignmentEntity(assignmentId));
    }

    // =========================================================
    // 12. GET STATUS HISTORY
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryStatusHistoryResponse> getHistory(Long assignmentId) {

        getAssignmentEntity(assignmentId);

        return historyRepository
                .findByDeliveryAssignmentIdOrderByCreatedAtAsc(assignmentId)
                .stream()
                .map(h -> DeliveryStatusHistoryResponse.builder()
                        .id(h.getId())
                        .assignmentId(assignmentId)
                        .status(h.getStatus().name())
                        .latitude(h.getLatitude())
                        .longitude(h.getLongitude())
                        .remarks(h.getRemarks())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private OtpResponse issueOtp(DeliveryAssignment assignment, DeliveryOtpType type) {

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        DeliveryOtp entity = DeliveryOtp.builder()
                .deliveryAssignment(assignment)
                .otpType(type)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(OffsetDateTime.now().plusMinutes(otpExpiryMinutes))
                .attemptCount(0)
                .build();

        otpRepository.save(entity);

        assignment.setStatus(type == DeliveryOtpType.PICKUP
                ? DeliveryAssignmentStatus.PICKUP_OTP_SENT
                : DeliveryAssignmentStatus.DELIVERY_OTP_SENT);
        assignmentRepository.save(assignment);

        saveHistory(assignment, assignment.getStatus(), getCurrentUser().getId(),
                type + " OTP generated");

        if (type == DeliveryOtpType.DELIVERY) {
            notificationService.createNotification(
                    assignment.getDeliveryRequest().getCustomer().getId(),
                    "Delivery OTP",
                    "Please share the delivery OTP with the delivery partner when your order arrives.",
                    "DELIVERY_OTP",
                    "DELIVERY_ASSIGNMENT",
                    assignment.getId()
            );
        }

        return new OtpResponse(
                assignment.getId(),
                type.name(),
                entity.getExpiresAt(),
                developmentOtpMode ? otp : null
        );
    }

    private void validateOtp(DeliveryOtp deliveryOtp, String otp) {

        if (deliveryOtp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        if (deliveryOtp.getAttemptCount() >= 5) {
            throw new RuntimeException("Maximum OTP attempts exceeded");
        }
        if (otp == null || otp.isBlank()) {
            deliveryOtp.setAttemptCount(deliveryOtp.getAttemptCount() + 1);
            otpRepository.save(deliveryOtp);
            throw new RuntimeException("OTP is required");
        }
        if (!passwordEncoder.matches(otp, deliveryOtp.getOtpHash())) {
            deliveryOtp.setAttemptCount(deliveryOtp.getAttemptCount() + 1);
            otpRepository.save(deliveryOtp);
            throw new RuntimeException("Invalid OTP");
        }
    }

    private void saveHistory(DeliveryAssignment assignment, DeliveryAssignmentStatus status,
                              Long userId, String remarks) {

        DeliveryStatusHistory history = DeliveryStatusHistory.builder()
                .deliveryAssignment(assignment)
                .status(status)
                .createdBy(userId)
                .remarks(remarks)
                .build();

        historyRepository.save(history);
    }

    private DeliveryAssignment getAssignmentEntity(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Delivery assignment not found"));
    }

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User is not authenticated");
        }

        return userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private DeliveryAssignmentResponse mapToResponse(DeliveryAssignment assignment) {

        DeliveryPartner partner = assignment.getPartner();
        User user = partner.getUser();
        DeliveryQuote quote = assignment.getQuote();

        return DeliveryAssignmentResponse.builder()
                .id(assignment.getId())
                .deliveryRequestId(assignment.getDeliveryRequest().getId())
                .quoteId(quote != null ? quote.getId() : null)
                .partnerId(partner.getId())
                .partnerUserId(user.getId())
                .partnerName(user.getFullName())
                .deliveryAmount(quote != null ? quote.getQuotedAmount() : null)
                .estimatedMinutes(quote != null ? quote.getEstimatedMinutes() : null)
                .status(assignment.getStatus().name())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .build();
    }
}