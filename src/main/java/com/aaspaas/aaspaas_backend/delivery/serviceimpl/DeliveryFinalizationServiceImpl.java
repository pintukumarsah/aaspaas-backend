package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryCompletionResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryCompletionFinalization;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryFinalizationRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.PartnerPayoutRepository;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryFinalizationService;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryFinalizationServiceImpl
        implements DeliveryFinalizationService {

    private final DeliveryAssignmentRepository assignmentRepository;

    private final DeliveryFinalizationRepository
            finalizationRepository;

    private final DeliveryPartnerRepository partnerRepository;

    private final PartnerPayoutRepository payoutRepository;

    private final OrderRepository orderRepository;


    /**
     * Finalizes the business side of a delivery AFTER
     * the existing delivery OTP verification has already
     * changed the assignment to DELIVERED.
     *
     * This method DOES NOT:
     *
     * - generate OTP
     * - verify OTP
     * - change PICKED_UP
     * - change OUT_FOR_DELIVERY
     * - change assignment to DELIVERED
     * - create a new commission
     * - create a new payout when one already exists
     *
     * It only finalizes dependent business records.
     */
    @Override
    @Transactional
    public DeliveryCompletionResponse finalizeDelivery(
            Long assignmentId,
            Long finalizedBy
    ) {

        if (assignmentId == null) {
            throw new IllegalArgumentException(
                    "Delivery assignment ID is required"
            );
        }

        /*
         * =====================================================
         * STEP 1
         * Lock the assignment.
         *
         * This protects against two concurrent completion
         * requests for the same delivery.
         * =====================================================
         */

        DeliveryAssignment assignment =
                assignmentRepository
                        .findByIdForUpdate(assignmentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Delivery assignment not found"
                                )
                        );


        /*
         * =====================================================
         * STEP 2
         * Idempotency check.
         *
         * If this delivery was already finalized, do not
         * create another completion record or modify payout
         * again.
         * =====================================================
         */

        var existingFinalization =
                finalizationRepository
                        .findByDeliveryAssignmentId(
                                assignmentId
                        );

        if (existingFinalization.isPresent()) {

            Order existingOrder =
                    orderRepository
                            .findById(
                                    assignment
                                            .getDeliveryRequest()
                                            .getOrder()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Order not found"
                                    )
                            );

            DeliveryPartner existingPartner =
                    assignment.getPartner();

            DeliveryCompletionFinalization finalization =
                    existingFinalization.get();

            return DeliveryCompletionResponse
                    .builder()
                    .assignmentId(
                            assignment.getId()
                    )
                    .orderId(
                            existingOrder.getId()
                    )
                    .partnerId(
                            existingPartner.getId()
                    )
                    .assignmentStatus(
                            assignment
                                    .getStatus()
                                    .name()
                    )
                    .orderStatus(
                            existingOrder
                                    .getOrderStatus()
                    )
                    .partnerAvailabilityStatus(
                            existingPartner
                                    .getAvailabilityStatus()
                                    .name()
                    )
                    .completionStatus(
                            finalization
                                    .getStatus()
                    )
                    .completedAt(
                            finalization
                                    .getFinalizedAt()
                    )
                    .alreadyFinalized(true)
                    .build();
        }


        /*
         * =====================================================
         * STEP 3
         * Safety check.
         *
         * Part 24 is allowed to run ONLY after the existing
         * OTP/state-machine flow has already reached DELIVERED.
         * =====================================================
         */

        if (assignment.getStatus()
                != DeliveryAssignmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Delivery can be finalized only after " +
                    "successful delivery completion"
            );
        }


        /*
         * =====================================================
         * STEP 4
         * Get the actual order.
         *
         * Actual relationship in your project:
         *
         * DeliveryAssignment
         *       ↓
         * DeliveryRequest
         *       ↓
         * Order
         * =====================================================
         */

        Long orderId =
                assignment
                        .getDeliveryRequest()
                        .getOrder()
                        .getId();


        /*
         * Lock order to prevent concurrent status changes.
         */

        Order order =
                orderRepository
                        .findByIdForUpdate(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Order not found"
                                )
                        );


        /*
         * =====================================================
         * STEP 5
         * Finalize order.
         *
         * Your Order entity stores orderStatus as String,
         * not an enum.
         * =====================================================
         */

        if (!"DELIVERED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            order.setOrderStatus(
                    "DELIVERED"
            );

            orderRepository.save(order);
        }


        /*
         * =====================================================
         * STEP 6
         * Partner.
         *
         * Existing verifyDeliveryOtp() already:
         *
         * AVAILABLE
         * totalDeliveries + 1
         *
         * Therefore Part 24 does NOT increment it again.
         *
         * We only use the existing partner information.
         * =====================================================
         */

        DeliveryPartner partner =
                partnerRepository
                        .findByIdForUpdate(
                                assignment
                                        .getPartner()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Delivery partner not found"
                                )
                        );


        /*
         * =====================================================
         * STEP 7
         * Payout.
         *
         * Part 22 already creates:
         *
         * PENDING payout
         *
         * Now delivery is successfully completed.
         *
         * Therefore:
         *
         * PENDING → ELIGIBLE
         *
         * We DO NOT create a second payout.
         * =====================================================
         */

        PartnerPayout payout =
                payoutRepository
                        .findByDeliveryAssignmentIdForUpdate(
                                assignmentId
                        )
                        .orElse(null);


        /*
         * Normally payout must already exist because Part 22
         * creates it.
         *
         * But if an old/legacy assignment has no payout,
         * create one from the immutable financial snapshot
         * stored on DeliveryAssignment.
         *
         * This keeps the completion flow financially consistent.
         */

        if (payout == null) {

            payout = new PartnerPayout();

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

            payout =
                    payoutRepository.save(
                            payout
                    );
        }


        /*
         * =====================================================
         * STEP 8
         * Move payout to ELIGIBLE.
         *
         * Do not downgrade:
         *
         * PROCESSING
         * PAID
         *
         * because those are already further in the payout
         * lifecycle.
         * =====================================================
         */

        if (payout.getStatus()
                == PartnerPayoutStatus.PENDING) {

            payout.setStatus(
                    PartnerPayoutStatus.ELIGIBLE
            );

            payoutRepository.save(payout);
        }


        /*
         * =====================================================
         * STEP 9
         * Create finalization record.
         *
         * UNIQUE(delivery_assignment_id) prevents duplicate
         * completion records at database level.
         * =====================================================
         */

        DeliveryCompletionFinalization finalization =
                new DeliveryCompletionFinalization();

        finalization.setDeliveryAssignmentId(
                assignment.getId()
        );

        finalization.setOrderId(
                order.getId()
        );

        finalization.setPartnerId(
                partner.getId()
        );

        finalization.setFinalizedBy(
                finalizedBy
        );

        finalization.setStatus(
                "COMPLETED"
        );

        finalization.setNotes(
                "Delivery successfully finalized after " +
                "delivery OTP verification"
        );

        finalization.setFinalizedAt(
                OffsetDateTime.now()
        );

        finalizationRepository.save(
                finalization
        );


        /*
         * =====================================================
         * STEP 10
         * Return final response.
         * =====================================================
         */

        return DeliveryCompletionResponse
                .builder()
                .assignmentId(
                        assignment.getId()
                )
                .orderId(
                        order.getId()
                )
                .partnerId(
                        partner.getId()
                )
                .assignmentStatus(
                        assignment
                                .getStatus()
                                .name()
                )
                .orderStatus(
                        order.getOrderStatus()
                )
                .partnerAvailabilityStatus(
                        partner
                                .getAvailabilityStatus()
                                .name()
                )
                .completionStatus(
                        finalization
                                .getStatus()
                )
                .completedAt(
                        finalization
                                .getFinalizedAt()
                )
                .alreadyFinalized(false)
                .build();
    }
}