package com.aaspaas.aaspaas_backend.cancellation.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.cancellation.dto.CancelOrderRequest;
import com.aaspaas.aaspaas_backend.cancellation.dto.CancellationResponse;
import com.aaspaas.aaspaas_backend.cancellation.entity.OrderCancellation;
import com.aaspaas.aaspaas_backend.cancellation.enums.CancellationStatus;
import com.aaspaas.aaspaas_backend.cancellation.repository.OrderCancellationRepository;
import com.aaspaas.aaspaas_backend.cancellation.service.OrderCancellationService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartnerAvailabilityStatus;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.PartnerPayoutRepository;
import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.payment.entity.Payment;
import com.aaspaas.aaspaas_backend.payment.entity.PaymentRefund;
import com.aaspaas.aaspaas_backend.payment.enums.PaymentStatus;
import com.aaspaas.aaspaas_backend.payment.enums.RefundStatus;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRefundRepository;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderCancellationServiceImpl
        implements OrderCancellationService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final OrderCancellationRepository
            cancellationRepository;

    private final DeliveryRequestRepository
            deliveryRequestRepository;

    private final DeliveryAssignmentRepository
            deliveryAssignmentRepository;

    private final PartnerPayoutRepository
            partnerPayoutRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentRefundRepository
            paymentRefundRepository;

    private final NotificationService notificationService;

    @Override
    @Transactional
    public CancellationResponse cancelOrder(
            Long orderId,
            CancelOrderRequest request,
            String authenticatedPhone
    ) {

        if (orderId == null) {
            throw new BusinessException(
                    "Order ID is required",
                    400
            );
        }

        User currentUser =
                userRepository
                        .findByPhone(authenticatedPhone)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "User not found",
                                        404
                                )
                        );

        /*
         * Pessimistic lock is extremely important here.
         *
         * Example:
         *
         * Customer clicks Cancel twice.
         *
         * Request-1 -> locks order
         * Request-2 -> waits
         *
         * Request-1 -> cancels
         * Request-2 -> sees CANCELLED
         *
         * Therefore duplicate refund/payout cancellation
         * is prevented.
         */
        Order order =
                orderRepository
                        .findByIdForUpdate(orderId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );

        String cancellationType =
                resolveCancellationType(
                        order,
                        currentUser
                );

        validateOrderCanBeCancelled(
                order,
                cancellationType
        );

        /*
         * Idempotency safety.
         */
        if (cancellationRepository
                .existsByOrderId(orderId)) {

            throw new BusinessException(
                    "Order has already been cancelled",
                    409
            );
        }

        /*
         * 1. Cancel delivery
         */
        boolean deliveryCancelled =
                cancelDelivery(
                        order,
                        currentUser.getId()
                );

        /*
         * 2. Handle payment/refund
         */
        RefundResult refundResult =
                handlePaymentRefund(
                        order,
                        request.getReason()
                );

        /*
         * 3. Change order state
         */
        order.setOrderStatus("CANCELLED");

        if (refundResult.refundCreated()) {

            if (refundResult.refundStatus()
                    == RefundStatus.SUCCESS) {

                order.setPaymentStatus("REFUNDED");

            } else {

                order.setPaymentStatus(
                        "REFUND_PENDING"
                );
            }

        } else if (refundResult.paymentStatus()
                != null) {

            order.setPaymentStatus(
                    refundResult.paymentStatus()
            );
        }

        order.setCancelledAt(
                OffsetDateTime.now()
        );

        order.setCancelledBy(
                currentUser.getId()
        );

        order.setCancellationReason(
                request.getReason().trim()
        );

        orderRepository.save(order);

        /*
         * 4. Create cancellation record
         */
        OrderCancellation cancellation =
                new OrderCancellation();

        cancellation.setOrder(order);

        cancellation.setCancelledBy(currentUser);

        cancellation.setCancelledByType(
                cancellationType
        );

        cancellation.setReason(
                request.getReason().trim()
        );

        if (refundResult.refundCreated()
                && refundResult.refundStatus()
                != RefundStatus.SUCCESS) {

            cancellation.setStatus(
                    CancellationStatus.REFUND_PENDING
            );

        } else if (refundResult.refundCreated()) {

            cancellation.setStatus(
                    CancellationStatus.REFUND_COMPLETED
            );

        } else {

            cancellation.setStatus(
                    CancellationStatus.COMPLETED
            );
        }

        cancellationRepository.save(cancellation);

        /*
         * 5. Customer notification
         */
        notificationService.createNotification(
                order.getCustomer().getId(),
                "Order Cancelled",
                "Your order "
                        + order.getOrderNumber()
                        + " has been cancelled.",
                "ORDER_CANCELLED",
                "ORDER",
                order.getId()
        );

        /*
         * 6. Seller notification
         */
        if (order.getBusiness() != null
                && order.getBusiness().getOwner() != null
                && !order.getBusiness()
                        .getOwner()
                        .getId()
                        .equals(currentUser.getId())) {

            notificationService.createNotification(
                    order.getBusiness()
                            .getOwner()
                            .getId(),
                    "Order Cancelled",
                    "Order "
                            + order.getOrderNumber()
                            + " has been cancelled.",
                    "ORDER_CANCELLED",
                    "ORDER",
                    order.getId()
            );
        }

        return CancellationResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .deliveryCancelled(deliveryCancelled)
                .refundCreated(
                        refundResult.refundCreated()
                )
                .refundAmount(
                        refundResult.refundAmount()
                )
                .refundStatus(
                        refundResult.refundStatus() != null
                                ? refundResult.refundStatus().name()
                                : null
                )
                .cancelledByType(cancellationType)
                .reason(request.getReason())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    private String resolveCancellationType(
            Order order,
            User currentUser
    ) {

        if (order.getCustomer()
                .getId()
                .equals(currentUser.getId())) {

            return "CUSTOMER";
        }

        Business business =
                order.getBusiness();

        if (business != null
                && business.getOwner() != null
                && business.getOwner()
                        .getId()
                        .equals(currentUser.getId())) {

            return "SELLER";
        }

        throw new BusinessException(
                "You are not allowed to cancel this order",
                403
        );
    }

    private void validateOrderCanBeCancelled(
            Order order,
            String cancellationType
    ) {

        String status =
                order.getOrderStatus() == null
                        ? ""
                        : order.getOrderStatus()
                                .toUpperCase(Locale.ROOT);

        if ("CANCELLED".equals(status)) {

            throw new BusinessException(
                    "Order is already cancelled",
                    409
            );
        }

        if ("DELIVERED".equals(status)) {

            throw new BusinessException(
                    "Delivered order cannot be cancelled",
                    409
            );
        }

        if ("REFUNDED".equals(status)) {

            throw new BusinessException(
                    "Refunded order cannot be cancelled again",
                    409
            );
        }

        /*
         * Customer and seller both cannot cancel after
         * physical pickup has happened.
         */
        if ("OUT_FOR_DELIVERY".equals(status)) {

            throw new BusinessException(
                    "Order cannot be cancelled after it is out for delivery",
                    409
            );
        }

        /*
         * READY_FOR_PICKUP is allowed for seller/customer
         * cancellation.
         *
         * Actual delivery assignment state is checked separately.
         */
    }

    private boolean cancelDelivery(
            Order order,
            Long cancelledBy
    ) {

        DeliveryRequest deliveryRequest =
                deliveryRequestRepository
                        .findByOrderId(order.getId())
                        .orElse(null);

        if (deliveryRequest == null) {
            return false;
        }

        DeliveryRequest lockedRequest =
                deliveryRequestRepository
                        .findByIdForUpdate(
                                deliveryRequest.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery request not found",
                                        404
                                )
                        );

        DeliveryAssignment assignment =
                deliveryAssignmentRepository
                        .findByDeliveryRequestId(
                                lockedRequest.getId()
                        )
                        .orElse(null);

        if (assignment == null) {

            lockedRequest.setStatus(
                    "CANCELLED"
            );

            deliveryRequestRepository.save(
                    lockedRequest
            );

            return true;
        }

        DeliveryAssignment lockedAssignment =
                deliveryAssignmentRepository
                        .findByIdForUpdate(
                                assignment.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery assignment not found",
                                        404
                                )
                        );

        DeliveryAssignmentStatus status =
                lockedAssignment.getStatus();

        if (status == DeliveryAssignmentStatus.DELIVERED) {

            throw new BusinessException(
                    "Delivery has already been completed",
                    409
            );
        }

        if (status == DeliveryAssignmentStatus.PICKED_UP
                || status == DeliveryAssignmentStatus.OUT_FOR_DELIVERY
                || status == DeliveryAssignmentStatus.DELIVERY_OTP_SENT) {

            throw new BusinessException(
                    "Order cannot be cancelled after item pickup",
                    409
            );
        }

        if (status != DeliveryAssignmentStatus.CANCELLED
                && status != DeliveryAssignmentStatus.REJECTED) {

            lockedAssignment.setStatus(
                    DeliveryAssignmentStatus.CANCELLED
            );

            deliveryAssignmentRepository.save(
                    lockedAssignment
            );
        }

        lockedRequest.setStatus("CANCELLED");

        deliveryRequestRepository.save(
                lockedRequest
        );

        /*
         * Release partner.
         */
        DeliveryPartner partner =
                lockedAssignment.getPartner();

        if (partner != null) {

            partner.setAvailabilityStatus(
                    DeliveryPartnerAvailabilityStatus.AVAILABLE
            );
        }

        /*
         * Cancel payout if it exists.
         *
         * IMPORTANT:
         * PAID payout must never be silently reversed.
         */
        PartnerPayout payout =
                partnerPayoutRepository
                        .findByDeliveryAssignmentId(
                                lockedAssignment.getId()
                        )
                        .orElse(null);

        if (payout != null) {

            if (payout.getStatus()
                    == PartnerPayoutStatus.PENDING
                    || payout.getStatus()
                    == PartnerPayoutStatus.ELIGIBLE) {

                payout.setStatus(
                        PartnerPayoutStatus.CANCELLED
                );

                partnerPayoutRepository.save(
                        payout
                );
            }
        }

        return true;
    }

    private RefundResult handlePaymentRefund(
            Order order,
            String reason
    ) {

        Payment payment =
                paymentRepository
                        .findFirstByOrderIdOrderByCreatedAtDesc(
                                order.getId()
                        )
                        .orElse(null);

        /*
         * No payment record.
         */
        if (payment == null) {

            return new RefundResult(
                    false,
                    null,
                    null,
                    null
            );
        }

        /*
         * COD does not require a refund.
         */
        if (payment.getStatus()
                == PaymentStatus.INITIATED
                || payment.getStatus()
                == PaymentStatus.PENDING
                || payment.getStatus()
                == PaymentStatus.FAILED
                || payment.getStatus()
                == PaymentStatus.CANCELLED) {

            return new RefundResult(
                    false,
                    null,
                    null,
                    payment.getStatus().name()
            );
        }

        /*
         * Already refunded.
         */
        if (payment.getStatus()
                == PaymentStatus.REFUNDED) {

            PaymentRefund existingRefund =
                    paymentRefundRepository
                            .findByPaymentId(
                                    payment.getId()
                            )
                            .orElse(null);

            return new RefundResult(
                    existingRefund != null,
                    existingRefund != null
                            ? existingRefund
                                .getRefundAmount()
                            : BigDecimal.ZERO,
                    existingRefund != null
                            ? existingRefund.getStatus()
                            : RefundStatus.SUCCESS,
                    "REFUNDED"
            );
        }

        /*
         * Only successful payment can be refunded.
         */
        if (payment.getStatus()
                != PaymentStatus.SUCCESS) {

            return new RefundResult(
                    false,
                    null,
                    null,
                    payment.getStatus().name()
            );
        }

        PaymentRefund existing =
                paymentRefundRepository
                        .findByPaymentId(
                                payment.getId()
                        )
                        .orElse(null);

        if (existing != null) {

            return new RefundResult(
                    true,
                    existing.getRefundAmount(),
                    existing.getStatus(),
                    "REFUND_PENDING"
            );
        }

        Payment lockedPayment =
                paymentRepository
                        .findByIdForUpdate(
                                payment.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Payment not found",
                                        404
                                )
                        );

        PaymentRefund refund =
                new PaymentRefund();

        refund.setPayment(lockedPayment);

        refund.setOrder(order);

        refund.setRefundAmount(
                lockedPayment.getAmount()
        );

        refund.setRefundReference(
                "REF-"
                        + order.getOrderNumber()
                        + "-"
                        + System.currentTimeMillis()
        );

        refund.setStatus(
                RefundStatus.INITIATED
        );

        refund.setReason(reason);

        paymentRefundRepository.save(refund);

        /*
         * We DO NOT mark gateway refund successful here.
         *
         * Actual gateway must confirm refund using
         * webhook/signature verification.
         */
        lockedPayment.setStatus(
                PaymentStatus.REFUNDED
        );

        paymentRepository.save(lockedPayment);

        return new RefundResult(
                true,
                refund.getRefundAmount(),
                refund.getStatus(),
                "REFUND_PENDING"
        );
    }

    private record RefundResult(
            boolean refundCreated,
            BigDecimal refundAmount,
            RefundStatus refundStatus,
            String paymentStatus
    ) {
    }
}