package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.service.DeliveryAssignmentService;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryAssignmentResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryQuoteRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentServiceImpl
        implements DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;

    private final DeliveryRequestRepository requestRepository;

    private final DeliveryQuoteRepository quoteRepository;

    private final DeliveryPartnerRepository partnerRepository;

    private final UserRepository userRepository;


    // =========================================================
    // CUSTOMER ACCEPTS DELIVERY QUOTE
    // =========================================================

    @Override
    @Transactional
    public DeliveryAssignmentResponse acceptQuote(Long quoteId) {

        String phone = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Find authenticated customer
        User customer = findUser(phone);


        /*
         * Lock the selected quote.
         *
         * This prevents two simultaneous requests
         * from accepting the same quote.
         */
        DeliveryQuote quote =
                quoteRepository.findByIdForUpdate(quoteId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery quote not found"
                                )
                        );


        /*
         * Lock delivery request.
         *
         * This is important because one delivery request
         * can have only one delivery partner.
         */
        DeliveryRequest deliveryRequest =
                requestRepository.findByIdForUpdate(
                        quote.getDeliveryRequest().getId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Delivery request not found"
                        )
                );


        // =====================================================
        // SECURITY CHECK
        // =====================================================

        /*
         * Only the customer who created the delivery request
         * can accept the quote.
         */
        if (!deliveryRequest.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to accept this quote"
            );
        }


        // =====================================================
        // REQUEST STATUS CHECK
        // =====================================================

        if (!"OPEN".equals(deliveryRequest.getStatus())) {

            throw new RuntimeException(
                    "Delivery request is no longer open"
            );
        }


        // =====================================================
        // QUOTE STATUS CHECK
        // =====================================================

        if (!"PENDING".equals(quote.getStatus())) {

            throw new RuntimeException(
                    "This quote is no longer available"
            );
        }


        // =====================================================
        // EXPIRY CHECK
        // =====================================================

        if (deliveryRequest.getExpiresAt() != null
                && deliveryRequest.getExpiresAt()
                .isBefore(OffsetDateTime.now())) {

            throw new RuntimeException(
                    "Delivery request has expired"
            );
        }


        // =====================================================
        // CHECK EXISTING ASSIGNMENT
        // =====================================================

        if (assignmentRepository
                .existsByDeliveryRequestId(
                        deliveryRequest.getId()
                )) {

            throw new RuntimeException(
                    "Delivery partner already assigned"
            );
        }


        // =====================================================
        // GET DELIVERY PARTNER
        // =====================================================

        DeliveryPartner partner = quote.getPartner();


        // =====================================================
        // ACCEPT SELECTED QUOTE
        // =====================================================

        quote.setStatus("ACCEPTED");

        quoteRepository.save(quote);


        // =====================================================
        // REJECT OTHER QUOTES
        // =====================================================

        List<DeliveryQuote> allQuotes =
                quoteRepository
                        .findByDeliveryRequestIdOrderByQuotedAmountAsc(
                                deliveryRequest.getId()
                        );

        for (DeliveryQuote otherQuote : allQuotes) {

            /*
             * Do not reject the selected quote.
             */
            if (!otherQuote.getId()
                    .equals(quote.getId())) {

                /*
                 * Only pending quotes should be rejected.
                 */
                if ("PENDING".equals(
                        otherQuote.getStatus())) {

                    otherQuote.setStatus("REJECTED");
                }
            }
        }

        quoteRepository.saveAll(allQuotes);


        // =====================================================
        // CREATE DELIVERY ASSIGNMENT
        // =====================================================

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


        // =====================================================
        // UPDATE DELIVERY REQUEST
        // =====================================================

        deliveryRequest.setStatus("ASSIGNED");

        requestRepository.save(deliveryRequest);


        // =====================================================
        // PARTNER STATUS
        // =====================================================

        /*
         * Partner is now busy because a delivery
         * has been assigned.
         */
        partner.setAvailabilityStatus("BUSY");

        partnerRepository.save(partner);


        // =====================================================
        // RETURN RESPONSE
        // =====================================================

        return mapToResponse(assignment);
    }


    // =========================================================
    // GET MY ASSIGNMENT - DELIVERY PARTNER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DeliveryAssignmentResponse getMyAssignment() {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();


        // Find delivery partner using logged-in user
        DeliveryPartner partner =
                partnerRepository
                        .findByUserPhone(phone)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner profile not found"
                                )
                        );


        /*
         * Currently fetching assignments.
         *
         * Later we will optimize this with a direct
         * repository query.
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
    // GET ASSIGNMENT BY ID
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
    // FIND USER
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
    // MAP ENTITY → RESPONSE
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