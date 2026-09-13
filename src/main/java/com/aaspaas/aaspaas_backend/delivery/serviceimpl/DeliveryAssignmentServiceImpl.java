package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.service.DeliveryAssignmentService;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryOtpResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryOtp;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryOtpRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryQuoteRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentServiceImpl
        implements DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;

    private final DeliveryRequestRepository requestRepository;

    private final DeliveryQuoteRepository quoteRepository;

    private final DeliveryPartnerRepository partnerRepository;

    private final DeliveryOtpRepository otpRepository;

    private final UserRepository userRepository;


    // =========================================================
    // 1. CUSTOMER ACCEPTS DELIVERY QUOTE
    // =========================================================

    @Override
    @Transactional
    public DeliveryAssignmentResponse acceptQuote(Long quoteId) {

        String phone = getLoggedInPhone();

        User customer = findUser(phone);


        // -----------------------------------------------------
        // Lock selected quote
        // -----------------------------------------------------

        DeliveryQuote quote =
                quoteRepository.findByIdForUpdate(quoteId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery quote not found"
                                )
                        );


        // -----------------------------------------------------
        // Lock delivery request
        // -----------------------------------------------------

        Long deliveryRequestId =
                quote.getDeliveryRequest().getId();

        DeliveryRequest deliveryRequest =
                requestRepository.findByIdForUpdate(
                        deliveryRequestId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Delivery request not found"
                        )
                );


        // -----------------------------------------------------
        // Security check
        // -----------------------------------------------------

        if (!deliveryRequest.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to accept this quote"
            );
        }


        // -----------------------------------------------------
        // Delivery request status
        // -----------------------------------------------------

        if (!"OPEN".equals(deliveryRequest.getStatus())) {

            throw new RuntimeException(
                    "Delivery request is no longer open"
            );
        }


        // -----------------------------------------------------
        // Quote status
        // -----------------------------------------------------

        if (!"PENDING".equals(quote.getStatus())) {

            throw new RuntimeException(
                    "This quote is no longer available"
            );
        }


        // -----------------------------------------------------
        // Expiry check
        // -----------------------------------------------------

        if (deliveryRequest.getExpiresAt() != null
                && deliveryRequest.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Delivery request has expired"
            );
        }


        // -----------------------------------------------------
        // Check existing assignment
        // -----------------------------------------------------

        if (assignmentRepository
                .existsByDeliveryRequestId(
                        deliveryRequest.getId()
                )) {

            throw new RuntimeException(
                    "Delivery partner already assigned"
            );
        }


        // -----------------------------------------------------
        // Get partner
        // -----------------------------------------------------

        DeliveryPartner partner =
                quote.getPartner();


        // -----------------------------------------------------
        // Make selected quote ACCEPTED
        // -----------------------------------------------------

        quote.setStatus("ACCEPTED");

        quoteRepository.save(quote);


        // -----------------------------------------------------
        // Reject all other pending quotes
        // -----------------------------------------------------

        List<DeliveryQuote> allQuotes =
                quoteRepository
                        .findByDeliveryRequestIdOrderByQuotedAmountAsc(
                                deliveryRequest.getId()
                        );

        for (DeliveryQuote otherQuote : allQuotes) {

            if (!otherQuote.getId()
                    .equals(quote.getId())) {

                if ("PENDING".equals(
                        otherQuote.getStatus())) {

                    otherQuote.setStatus("REJECTED");
                }
            }
        }

        quoteRepository.saveAll(allQuotes);


        // -----------------------------------------------------
        // Create assignment
        // -----------------------------------------------------

        DeliveryAssignment assignment =
                DeliveryAssignment.builder()
                        .deliveryRequest(deliveryRequest)
                        .partner(partner)
                        .quote(quote)
                        .status("ASSIGNED")
                        .assignedAt(
                                OffsetDateTime.now()
                        )
                        .build();

        assignment =
                assignmentRepository.save(
                        assignment
                );


        // -----------------------------------------------------
        // Update delivery request
        // -----------------------------------------------------

        deliveryRequest.setStatus("ASSIGNED");

        requestRepository.save(deliveryRequest);


        // -----------------------------------------------------
        // Partner becomes BUSY
        // -----------------------------------------------------

        partner.setAvailabilityStatus("BUSY");

        partnerRepository.save(partner);


        return mapToResponse(assignment);
    }


    // =========================================================
    // 2. DELIVERY PARTNER ACCEPTS ASSIGNMENT
    // =========================================================

    @Override
    @Transactional
    public DeliveryAssignmentResponse acceptAssignment(
            Long assignmentId) {

        String phone = getLoggedInPhone();


        // -----------------------------------------------------
        // Find partner
        // -----------------------------------------------------

        DeliveryPartner partner =
                partnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );


        // -----------------------------------------------------
        // Find assignment
        // -----------------------------------------------------

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        // -----------------------------------------------------
        // Verify assignment belongs to this partner
        // -----------------------------------------------------

        if (!assignment.getPartner()
                .getId()
                .equals(partner.getId())) {

            throw new RuntimeException(
                    "You are not assigned to this delivery"
            );
        }


        // -----------------------------------------------------
        // Status check
        // -----------------------------------------------------

        if (!"ASSIGNED".equals(
                assignment.getStatus())) {

            throw new RuntimeException(
                    "Assignment cannot be accepted in current status"
            );
        }


        // -----------------------------------------------------
        // Accept assignment
        // -----------------------------------------------------

        assignment.setStatus("ACCEPTED");

        assignment.setAcceptedAt(
                OffsetDateTime.now()
        );

        assignment =
                assignmentRepository.save(
                        assignment
                );


        return mapToResponse(assignment);
    }


    // =========================================================
    // 3. GENERATE PICKUP OTP
    // =========================================================

    @Override
    @Transactional
    public DeliveryOtpResponse generatePickupOtp(
            Long assignmentId) {

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        // -----------------------------------------------------
        // Status check
        // -----------------------------------------------------

        if (!"ACCEPTED".equals(
                assignment.getStatus())) {

            throw new RuntimeException(
                    "Partner must accept assignment first"
            );
        }


        // -----------------------------------------------------
        // Generate OTP
        // -----------------------------------------------------

        String otp = generateOtp();


        DeliveryOtp deliveryOtp =
                DeliveryOtp.builder()
                        .deliveryAssignment(assignment)
                        .otpType("PICKUP")
                        .otpHash(hashOtp(otp))
                        .expiresAt(
                                OffsetDateTime.now()
                                        .plusMinutes(10)
                        )
                        .attemptCount(0)
                        .build();


        deliveryOtp =
                otpRepository.save(
                        deliveryOtp
                );


        return DeliveryOtpResponse.builder()
                .assignmentId(assignmentId)
                .otpType("PICKUP")
                .otp(otp)
                .expiresAt(
                        deliveryOtp.getExpiresAt()
                )
                .build();
    }


    // =========================================================
    // 4. VERIFY PICKUP OTP
    // =========================================================

    @Override
    @Transactional
    public DeliveryAssignmentResponse verifyPickupOtp(
            Long assignmentId,
            String otp) {

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        // -----------------------------------------------------
        // Status check
        // -----------------------------------------------------

        if (!"ACCEPTED".equals(
                assignment.getStatus())) {

            throw new RuntimeException(
                    "Pickup OTP cannot be verified now"
            );
        }


        // -----------------------------------------------------
        // Find latest active pickup OTP
        // -----------------------------------------------------

        DeliveryOtp deliveryOtp =
                otpRepository
                        .findTopByDeliveryAssignmentIdAndOtpTypeAndVerifiedAtIsNullOrderByCreatedAtDesc(
                                assignmentId,
                                "PICKUP"
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pickup OTP not found"
                                )
                        );


        // -----------------------------------------------------
        // Expiry check
        // -----------------------------------------------------

        if (deliveryOtp.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Pickup OTP has expired"
            );
        }


        // -----------------------------------------------------
        // Attempt limit
        // -----------------------------------------------------

        if (deliveryOtp.getAttemptCount() >= 5) {

            throw new RuntimeException(
                    "Maximum OTP attempts exceeded"
            );
        }


        // -----------------------------------------------------
        // Null / blank OTP check
        // -----------------------------------------------------

        if (otp == null || otp.isBlank()) {

            deliveryOtp.setAttemptCount(
                    deliveryOtp.getAttemptCount() + 1
            );

            otpRepository.save(deliveryOtp);

            throw new RuntimeException(
                    "OTP is required"
            );
        }


        // -----------------------------------------------------
        // Verify OTP
        // -----------------------------------------------------

        if (!hashOtp(otp)
                .equals(deliveryOtp.getOtpHash())) {

            deliveryOtp.setAttemptCount(
                    deliveryOtp.getAttemptCount() + 1
            );

            otpRepository.save(deliveryOtp);

            throw new RuntimeException(
                    "Invalid pickup OTP"
            );
        }


        // -----------------------------------------------------
        // Mark OTP verified
        // -----------------------------------------------------

        deliveryOtp.setVerifiedAt(
                OffsetDateTime.now()
        );

        otpRepository.save(deliveryOtp);


        // -----------------------------------------------------
        // Update assignment
        // -----------------------------------------------------

        assignment.setStatus("PICKED_UP");

        assignment.setPickedUpAt(
                OffsetDateTime.now()
        );

        assignment =
                assignmentRepository.save(
                        assignment
                );


        return mapToResponse(assignment);
    }


    // =========================================================
    // 5. GENERATE DELIVERY OTP
    // =========================================================

    @Override
    @Transactional
    public DeliveryOtpResponse generateDeliveryOtp(
            Long assignmentId) {

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        // -----------------------------------------------------
        // Item must already be picked up
        // -----------------------------------------------------

        if (!"PICKED_UP".equals(
                assignment.getStatus())) {

            throw new RuntimeException(
                    "Item must be picked up first"
            );
        }


        // -----------------------------------------------------
        // Generate OTP
        // -----------------------------------------------------

        String otp = generateOtp();


        DeliveryOtp deliveryOtp =
                DeliveryOtp.builder()
                        .deliveryAssignment(assignment)
                        .otpType("DELIVERY")
                        .otpHash(hashOtp(otp))
                        .expiresAt(
                                OffsetDateTime.now()
                                        .plusMinutes(10)
                        )
                        .attemptCount(0)
                        .build();


        deliveryOtp =
                otpRepository.save(
                        deliveryOtp
                );


        return DeliveryOtpResponse.builder()
                .assignmentId(assignmentId)
                .otpType("DELIVERY")
                .otp(otp)
                .expiresAt(
                        deliveryOtp.getExpiresAt()
                )
                .build();
    }


    // =========================================================
    // 6. VERIFY DELIVERY OTP
    // =========================================================

    @Override
    @Transactional
    public DeliveryAssignmentResponse verifyDeliveryOtp(
            Long assignmentId,
            String otp) {

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        // -----------------------------------------------------
        // Status check
        // -----------------------------------------------------

        if (!"PICKED_UP".equals(assignment.getStatus())
        && !"OUT_FOR_DELIVERY".equals(
                assignment.getStatus())) {

            throw new RuntimeException(
                    "Delivery OTP cannot be verified now"
            );
        }


        // -----------------------------------------------------
        // Find latest active delivery OTP
        // -----------------------------------------------------

        DeliveryOtp deliveryOtp =
                otpRepository
                        .findTopByDeliveryAssignmentIdAndOtpTypeAndVerifiedAtIsNullOrderByCreatedAtDesc(
                                assignmentId,
                                "DELIVERY"
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery OTP not found"
                                )
                        );


        // -----------------------------------------------------
        // Expiry check
        // -----------------------------------------------------

        if (deliveryOtp.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Delivery OTP has expired"
            );
        }


        // -----------------------------------------------------
        // Attempt limit
        // -----------------------------------------------------

        if (deliveryOtp.getAttemptCount() >= 5) {

            throw new RuntimeException(
                    "Maximum OTP attempts exceeded"
            );
        }


        // -----------------------------------------------------
        // Null / blank OTP check
        // -----------------------------------------------------

        if (otp == null || otp.isBlank()) {

            deliveryOtp.setAttemptCount(
                    deliveryOtp.getAttemptCount() + 1
            );

            otpRepository.save(deliveryOtp);

            throw new RuntimeException(
                    "OTP is required"
            );
        }


        // -----------------------------------------------------
        // Verify OTP
        // -----------------------------------------------------

        if (!hashOtp(otp)
                .equals(deliveryOtp.getOtpHash())) {

            deliveryOtp.setAttemptCount(
                    deliveryOtp.getAttemptCount() + 1
            );

            otpRepository.save(deliveryOtp);

            throw new RuntimeException(
                    "Invalid delivery OTP"
            );
        }


        // -----------------------------------------------------
        // Mark OTP verified
        // -----------------------------------------------------

        deliveryOtp.setVerifiedAt(
                OffsetDateTime.now()
        );

        otpRepository.save(deliveryOtp);


        // -----------------------------------------------------
        // Mark delivery as DELIVERED
        // -----------------------------------------------------

        assignment.setStatus("DELIVERED");

        assignment.setDeliveredAt(
                OffsetDateTime.now()
        );

        assignment =
                assignmentRepository.save(
                        assignment
                );


        // -----------------------------------------------------
        // Partner becomes ONLINE again
        // -----------------------------------------------------

        DeliveryPartner partner =
                assignment.getPartner();

        partner.setAvailabilityStatus("ONLINE");

        Integer totalDeliveries =
                partner.getTotalDeliveries();

        if (totalDeliveries == null) {
            totalDeliveries = 0;
        }

        partner.setTotalDeliveries(
                totalDeliveries + 1
        );

        partnerRepository.save(partner);


        return mapToResponse(assignment);
    }


    // =========================================================
    // 7. GET MY ASSIGNMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DeliveryAssignmentResponse getMyAssignment() {

        String phone = getLoggedInPhone();


        DeliveryPartner partner =
                partnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );


        /*
         * Development version.
         *
         * Later we will replace this with a direct
         * database query.
         */
        DeliveryAssignment assignment =
                assignmentRepository
                        .findAll()
                        .stream()
                        .filter(a ->
                                a.getPartner()
                                        .getId()
                                        .equals(
                                                partner.getId()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No delivery assignment found"
                                )
                        );


        return mapToResponse(assignment);
    }


    // =========================================================
    // 8. GET ASSIGNMENT BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DeliveryAssignmentResponse getAssignment(
            Long assignmentId) {

        DeliveryAssignment assignment =
                assignmentRepository
                        .findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery assignment not found"
                                )
                        );


        return mapToResponse(assignment);
    }


    // =========================================================
    // 9. GET LOGGED-IN USER PHONE
    // =========================================================

    private String getLoggedInPhone() {

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();


        if (phone == null || phone.isBlank()) {

            throw new RuntimeException(
                    "Authenticated user phone not found"
            );
        }


        return phone;
    }


    // =========================================================
    // 10. FIND USER
    // =========================================================

    private User findUser(String phone) {

        return userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }


    // =========================================================
    // 11. GENERATE 6 DIGIT OTP
    // =========================================================

    private String generateOtp() {

        Random random = new Random();

        int otp =
                100000 +
                random.nextInt(900000);

        return String.valueOf(otp);
    }


    // =========================================================
    // 12. HASH OTP
    // =========================================================

    private String hashOtp(String otp) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");


            byte[] hash =
                    digest.digest(
                            otp.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            StringBuilder hexString =
                    new StringBuilder();


            for (byte b : hash) {

                String hex =
                        Integer.toHexString(
                                0xff & b
                        );


                if (hex.length() == 1) {
                    hexString.append('0');
                }


                hexString.append(hex);
            }


            return hexString.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to hash OTP",
                    e
            );
        }
    }


    // =========================================================
    // 13. ENTITY → RESPONSE
    // =========================================================

    private DeliveryAssignmentResponse mapToResponse(
            DeliveryAssignment assignment) {

        DeliveryPartner partner =
                assignment.getPartner();

        User user =
                partner.getUser();

        DeliveryQuote quote =
                assignment.getQuote();


        return DeliveryAssignmentResponse.builder()

                .id(
                        assignment.getId()
                )

                .deliveryRequestId(
                        assignment
                                .getDeliveryRequest()
                                .getId()
                )

                .quoteId(
                        quote.getId()
                )

                .partnerId(
                        partner.getId()
                )

                .partnerUserId(
                        user.getId()
                )

                .partnerName(
                        user.getFullName()
                )

                .deliveryAmount(
                        quote.getQuotedAmount()
                )

                .estimatedMinutes(
                        quote.getEstimatedMinutes()
                )

                .status(
                        assignment.getStatus()
                )

                .assignedAt(
                        assignment.getAssignedAt()
                )

                .acceptedAt(
                        assignment.getAcceptedAt()
                )

                .pickedUpAt(
                        assignment.getPickedUpAt()
                )

                .deliveredAt(
                        assignment.getDeliveredAt()
                )

                .build();
    }
}