package com.aaspaas.aaspaas_backend.order.dispute.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.order.dispute.dto.DisputeRefundExecutionResponse;
import com.aaspaas.aaspaas_backend.order.dispute.entity.OrderDispute;
import com.aaspaas.aaspaas_backend.order.dispute.repository.OrderDisputeRepository;
import com.aaspaas.aaspaas_backend.order.dispute.service.DisputeRefundService;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRefundRepository;
import com.aaspaas.aaspaas_backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connects an already RESOLVED dispute (resolutionCode REFUND_RECOMMENDED) to the
 * existing refund pipeline: PaymentService.initiateRefund -> Razorpay -> webhook.
 * Nothing in the existing dispute module is modified.
 *
 * Only a FULL refund is executed, because initiateRefund refunds the whole payment.
 * Partial refunds must be done manually in the gateway dashboard.
 */
@Service
@RequiredArgsConstructor
public class DisputeRefundServiceImpl implements DisputeRefundService {

    private final OrderDisputeRepository disputeRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public DisputeRefundExecutionResponse executeRefund(Long disputeId) {
        if (disputeId == null || disputeId <= 0) {
            throw new BusinessException("A valid dispute ID is required", 400);
        }

        // Row lock: two admins clicking at once cannot both pass the checks below.
        OrderDispute d = disputeRepository.findByIdForUpdate(disputeId)
                .orElseThrow(() -> new BusinessException("Dispute not found", 404));

        if (!"RESOLVED".equals(d.getStatus())) {
            throw new BusinessException("Dispute must be RESOLVED before a refund is executed", 409);
        }
        if (!"REFUND_RECOMMENDED".equals(d.getResolutionCode())) {
            throw new BusinessException(
                    "Only a REFUND_RECOMMENDED resolution can be executed automatically", 409);
        }
        if (d.getApprovedRefundAmount() == null
                || d.getApprovedRefundAmount().compareTo(d.getOrder().getTotalAmount()) != 0) {
            throw new BusinessException(
                    "Automatic execution supports full refunds only. "
                            + "Refund a partial amount manually in the payment gateway.", 409);
        }

        Long orderId = d.getOrder().getId();
        if (paymentRefundRepository.existsByOrderId(orderId)) {
            throw new BusinessException("A refund already exists for this order", 409);
        }

        // Existing pipeline: validates the payment is SUCCESS, calls the gateway,
        // marks PaymentRefund PROCESSING and waits for the webhook for the final state.
        paymentService.initiateRefund(orderId, "Dispute #" + d.getId());

        return DisputeRefundExecutionResponse.builder()
                .disputeId(d.getId())
                .orderId(orderId)
                .refundAmount(d.getApprovedRefundAmount())
                .refundState("SENT_TO_GATEWAY")
                .build();
    }
}
