package com.aaspaas.aaspaas_backend.delivery.serviceimpl;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryQuoteService;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.common.exception.ResourceNotFoundException;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryQuoteSelectionResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryQuote;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryQuoteStatus;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryQuoteRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.PartnerPayoutRepository;
import com.aaspaas.aaspaas_backend.delivery.pricing.service.DeliveryPricingService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryQuoteServiceImpl
        implements DeliveryQuoteService {

    private final DeliveryQuoteRepository deliveryQuoteRepository;

    private final DeliveryRequestRepository deliveryRequestRepository;

    private final DeliveryAssignmentRepository
            deliveryAssignmentRepository;

    private final DeliveryPartnerRepository
            deliveryPartnerRepository;

    private final PartnerPayoutRepository
            partnerPayoutRepository;

    private final DeliveryPricingService
            deliveryPricingService;

    private final UserRepository userRepository;


    @Override
    @Transactional
    public DeliveryQuoteSelectionResponse selectQuote(
            Long quoteId
    ) {

        User customer = getAuthenticatedUser();

        /*
         * Step 1:
         * Lock the quote.
         */
        DeliveryQuote quote =
                deliveryQuoteRepository
                        .findByIdForUpdate(quoteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery quote not found"
                                )
                        );

        /*
         * Step 2:
         * Lock the delivery request.
         */
        Long requestId =
                quote.getDeliveryRequest().getId();

        DeliveryRequest request =
                deliveryRequestRepository
                        .findByIdForUpdate(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery request not found"
                                )
                        );

        /*
         * Step 3:
         * Verify customer ownership.
         */
        validateCustomerOwnership(
                request,
                customer
        );

        /*
         * Step 4:
         * Validate request.
         */
        validateDeliveryRequest(request);

        /*
         * Step 5:
         * Validate selected quote.
         */
        validateQuote(
                quote,
                request
        );

        /*
         * Step 6:
         * Lock partner.
         */
        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByIdForUpdate(
                                quote.getPartner().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Delivery partner not found"
                                )
                        );

        /*
         * Step 7:
         * Validate partner.
         */
        validatePartner(
                partner
        );

        /*
         * Step 8:
         * Calculate final financial values.
         */
        BigDecimal agreedFee =
                quote.getQuotedAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal commission =
                deliveryPricingService
                        .calculatePlatformCommission(
                                agreedFee,
                                request.getDeliveryMode()
                        );

        BigDecimal partnerEarning =
                agreedFee
                        .subtract(commission)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        /*
         * Step 9:
         * Create assignment.
         */
        DeliveryAssignment assignment =
                createAssignment(
                        request,
                        quote,
                        partner,
                        agreedFee,
                        commission,
                        partnerEarning
                );

        /*
         * Step 10:
         * Mark selected quote.
         */
        quote.setStatus(
                DeliveryQuoteStatus.ACCEPTED
        );

        /*
         * Step 11:
         * Cancel remaining quotes.
         */
        deliveryQuoteRepository
                .cancelOtherPendingQuotes(
                        request.getId(),
                        quote.getId(),
                        DeliveryQuoteStatus.PENDING,
                        DeliveryQuoteStatus.CANCELLED
                );

        /*
         * Step 12:
         * Update delivery request.
         */
        request.setStatus("ASSIGNED");

        deliveryRequestRepository.save(
                request
        );

        /*
         * Step 13:
         * Mark partner busy.
         */
        markPartnerBusy(
                partner
        );

        /*
         * Step 14:
         * Create pending payout.
         */
        createPendingPayout(
                assignment,
                partner
        );

        /*
         * Transaction commits only if
         * all operations succeed.
         */
        return DeliveryQuoteSelectionResponse
                .builder()
                .deliveryRequestId(
                        request.getId()
                )
                .quoteId(
                        quote.getId()
                )
                .assignmentId(
                        assignment.getId()
                )
                .partnerId(
                        partner.getId()
                )
                .agreedDeliveryFee(
                        agreedFee
                )
                .platformCommission(
                        commission
                )
                .partnerEarning(
                        partnerEarning
                )
                .currency("INR")
                .assignmentStatus(
                        assignment
                                .getStatus()
                                .name()
                )
                .build();
    }


    private DeliveryAssignment createAssignment(
            DeliveryRequest request,
            DeliveryQuote quote,
            DeliveryPartner partner,
            BigDecimal agreedFee,
            BigDecimal commission,
            BigDecimal partnerEarning
    ) {

        if (deliveryAssignmentRepository
                .findByDeliveryRequestId(
                        request.getId()
                )
                .isPresent()) {

            throw new BusinessException(
                    "Delivery request is already assigned"
            );
        }

        DeliveryAssignment assignment =
                new DeliveryAssignment();

        assignment.setDeliveryRequest(
                request
        );

        assignment.setQuote(
                quote
        );

        assignment.setPartner(
                partner
        );

        assignment.setStatus(
                DeliveryAssignmentStatus.ASSIGNED
        );

        assignment.setAssignedAt(
                OffsetDateTime.now()
        );

        assignment.setAgreedDeliveryFee(
                agreedFee
        );

        assignment.setPlatformCommissionAmount(
                commission
        );

        assignment.setPartnerEarningAmount(
                partnerEarning
        );

        assignment.setCurrency(
                "INR"
        );

        return deliveryAssignmentRepository
                .save(assignment);
    }


    private void createPendingPayout(
            DeliveryAssignment assignment,
            DeliveryPartner partner
    ) {

        if (partnerPayoutRepository
                .existsByDeliveryAssignmentId(
                        assignment.getId()
                )) {

            return;
        }

        PartnerPayout payout =
                new PartnerPayout();

        payout.setPartner(
                partner
        );

        payout.setDeliveryAssignment(
                assignment
        );

        payout.setGrossAmount(
                assignment
                        .getAgreedDeliveryFee()
        );

        payout.setPlatformFee(
                assignment
                        .getPlatformCommissionAmount()
        );

        payout.setNetAmount(
                assignment
                        .getPartnerEarningAmount()
        );

        payout.setStatus(
                PartnerPayoutStatus.PENDING
        );

        payout.setCreatedAt(
                OffsetDateTime.now()
        );

        partnerPayoutRepository.save(
                payout
        );
    }


    private void validateCustomerOwnership(
            DeliveryRequest request,
            User customer
    ) {

        if (!request.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new BusinessException(
                    "You are not authorized to select a quote for this request"
            );
        }
    }


    private void validateDeliveryRequest(
            DeliveryRequest request
    ) {

        if (!"OPEN".equalsIgnoreCase(
                request.getStatus()
        )) {

            throw new BusinessException(
                    "Delivery request is not open"
            );
        }

        if (request.getExpiresAt() != null
                && request.getExpiresAt()
                .isBefore(
                        OffsetDateTime.now()
                )) {

            throw new BusinessException(
                    "Delivery request has expired"
            );
        }

        if (deliveryAssignmentRepository
                .findByDeliveryRequestId(
                        request.getId()
                )
                .isPresent()) {

            throw new BusinessException(
                    "Delivery request is already assigned"
            );
        }
    }


    private void validateQuote(
            DeliveryQuote quote,
            DeliveryRequest request
    ) {

        if (!quote.getDeliveryRequest()
                .getId()
                .equals(request.getId())) {

            throw new BusinessException(
                    "Quote does not belong to this delivery request"
            );
        }

        if (quote.getStatus()
                != DeliveryQuoteStatus.PENDING) {

            throw new BusinessException(
                    "Quote is no longer available"
            );
        }

        if (quote.getQuotedAmount() == null
                || quote.getQuotedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Invalid quote amount"
            );
        }

        if (request.getMaxBudget() != null
                && quote.getQuotedAmount()
                .compareTo(
                        request.getMaxBudget()
                ) > 0) {

            throw new BusinessException(
                    "Quote exceeds customer maximum budget"
            );
        }
    }


    private void validatePartner(
            DeliveryPartner partner
    ) {

        /*
         * IMPORTANT:
         * Replace this comparison with your
         * existing availability enum if your
         * DeliveryPartner entity uses enum.
         */
        if (!"AVAILABLE".equalsIgnoreCase(
                String.valueOf(
                        partner.getAvailabilityStatus()
                )
        )) {

            throw new BusinessException(
                    "Delivery partner is not available"
            );
        }

        long activeAssignments =
                deliveryAssignmentRepository
                        .countActiveAssignmentsForPartner(
                                partner.getId()
                        );

        if (activeAssignments > 0) {

            throw new BusinessException(
                    "Delivery partner already has an active delivery"
            );
        }
    }


    private void markPartnerBusy(
            DeliveryPartner partner
    ) {

        /*
         * If availabilityStatus is an enum,
         * replace "BUSY" with your enum constant.
         */
        partner.setAvailabilityStatus(
                com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus.BUSY
        );

        deliveryPartnerRepository.save(
                partner
        );
    }


    private User getAuthenticatedUser() {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<DeliveryQuote> getQuotesForRequest(
            Long deliveryRequestId
    ) {

        return deliveryQuoteRepository
                .findByDeliveryRequestIdOrderByQuotedAmountAsc(
                        deliveryRequestId
                );
    }


    @Override
    @Transactional
    public DeliveryQuote createQuote(
            Long deliveryRequestId,
            Long partnerId,
            BigDecimal quotedAmount,
            Integer estimatedMinutes,
            String message
    ) {

        throw new UnsupportedOperationException(
                "Use the existing production quote creation flow from Part 11 and integrate validation there."
        );
    }
}