package com.aaspaas.aaspaas_backend.order.dispute.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.order.dispute.dto.CreateOrderDisputeRequest;
import com.aaspaas.aaspaas_backend.order.dispute.dto.OrderDisputeResponse;
import com.aaspaas.aaspaas_backend.order.dispute.dto.ResolveOrderDisputeRequest;
import com.aaspaas.aaspaas_backend.order.dispute.entity.OrderDispute;
import com.aaspaas.aaspaas_backend.order.dispute.repository.OrderDisputeRepository;
import com.aaspaas.aaspaas_backend.order.dispute.service.OrderDisputeService;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderDisputeServiceImpl implements OrderDisputeService {
    private static final Set<String> ACTIVE_STATUSES = Set.of("OPEN", "UNDER_REVIEW");
    private static final Set<String> ALLOWED_FILTERS = Set.of("OPEN", "UNDER_REVIEW", "RESOLVED", "REJECTED", "CLOSED");
    private final OrderDisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderDisputeResponse raiseDispute(Long orderId, String authenticatedPhone, CreateOrderDisputeRequest request) {
        if (orderId == null || orderId <= 0) throw new BusinessException("A valid order ID is required", 400);
        User user = userRepository.findByPhone(authenticatedPhone)
                .orElseThrow(() -> new BusinessException("Authenticated user was not found", 404));
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("Order not found", 404));
        if (!order.getCustomer().getId().equals(user.getId())) throw new BusinessException("You can only raise a dispute for your own order", 403);
        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())) throw new BusinessException("A dispute cannot be raised for a cancelled order", 409);
        if (disputeRepository.existsByOrderIdAndRaisedByIdAndStatusIn(orderId, user.getId(), ACTIVE_STATUSES)) {
            throw new BusinessException("An open dispute already exists for this order", 409);
        }
        if (request.getRequestedRefundAmount() != null && request.getRequestedRefundAmount().compareTo(order.getTotalAmount()) > 0) {
            throw new BusinessException("Requested refund cannot exceed the order total", 400);
        }
        OrderDispute dispute = new OrderDispute();
        dispute.setOrder(order);
        dispute.setRaisedBy(user);
        dispute.setReasonCode(request.getReasonCode().trim().toUpperCase(Locale.ROOT));
        dispute.setDescription(request.getDescription().trim());
        dispute.setRequestedRefundAmount(request.getRequestedRefundAmount());
        dispute.setCurrency("INR");
        dispute.setStatus("OPEN");
        return OrderDisputeResponse.fromEntity(disputeRepository.save(dispute));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDisputeResponse> getAdminDisputes(String status, int page, int size) {
        validatePage(page, size);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderDispute> result;
        if (status == null || status.isBlank()) result = disputeRepository.findAllByOrderByCreatedAtDesc(pageable);
        else {
            String normalized = status.trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_FILTERS.contains(normalized)) throw new BusinessException("Unsupported dispute status", 400);
            result = disputeRepository.findByStatusOrderByCreatedAtDesc(normalized, pageable);
        }
        return result.map(OrderDisputeResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDisputeResponse getAdminDispute(Long disputeId) {
        return OrderDisputeResponse.fromEntity(findDispute(disputeId));
    }

    @Override
    @Transactional
    public OrderDisputeResponse startReview(Long disputeId, Long adminUserId) {
        OrderDispute dispute = findDisputeForUpdate(disputeId);
        if (!"OPEN".equals(dispute.getStatus())) throw new BusinessException("Only an open dispute can enter review", 409);
        dispute.setStatus("UNDER_REVIEW");
        dispute.setReviewedByUserId(adminUserId);
        dispute.setReviewedAt(OffsetDateTime.now());
        return OrderDisputeResponse.fromEntity(disputeRepository.save(dispute));
    }

    @Override
    @Transactional
    public OrderDisputeResponse resolve(Long disputeId, Long adminUserId, ResolveOrderDisputeRequest request) {
        OrderDispute dispute = findDisputeForUpdate(disputeId);
        if (!ACTIVE_STATUSES.contains(dispute.getStatus())) throw new BusinessException("Only an open or under-review dispute can be resolved", 409);
        String code = request.getResolutionCode().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("NO_ACTION", "REFUND_RECOMMENDED", "PARTIAL_REFUND_RECOMMENDED", "REPLACEMENT", "OTHER").contains(code)) {
            throw new BusinessException("Unsupported resolutionCode", 400);
        }
        BigDecimal approved = request.getApprovedRefundAmount();
        if (code.equals("REFUND_RECOMMENDED") && approved == null) throw new BusinessException("approvedRefundAmount is required for REFUND_RECOMMENDED", 400);
        if (approved != null && (dispute.getRequestedRefundAmount() == null || approved.compareTo(dispute.getRequestedRefundAmount()) > 0)) {
            throw new BusinessException("Approved refund cannot exceed the requested refund amount", 400);
        }
        if (approved != null && approved.compareTo(dispute.getOrder().getTotalAmount()) > 0) throw new BusinessException("Approved refund cannot exceed the order total", 400);
        dispute.setResolutionCode(code);
        dispute.setResolutionNotes(request.getResolutionNotes().trim());
        dispute.setApprovedRefundAmount(approved);
        dispute.setStatus("RESOLVED");
        dispute.setReviewedByUserId(adminUserId);
        if (dispute.getReviewedAt() == null) dispute.setReviewedAt(OffsetDateTime.now());
        dispute.setResolvedAt(OffsetDateTime.now());
        return OrderDisputeResponse.fromEntity(disputeRepository.save(dispute));
    }

    @Override
    @Transactional
    public OrderDisputeResponse reject(Long disputeId, Long adminUserId, String notes) {
        if (notes == null || notes.trim().length() < 10 || notes.trim().length() > 2000) throw new BusinessException("Rejection notes must be between 10 and 2000 characters", 400);
        OrderDispute dispute = findDisputeForUpdate(disputeId);
        if (!ACTIVE_STATUSES.contains(dispute.getStatus())) throw new BusinessException("Only an open or under-review dispute can be rejected", 409);
        dispute.setStatus("REJECTED");
        dispute.setResolutionCode("NO_ACTION");
        dispute.setResolutionNotes(notes.trim());
        dispute.setReviewedByUserId(adminUserId);
        if (dispute.getReviewedAt() == null) dispute.setReviewedAt(OffsetDateTime.now());
        dispute.setResolvedAt(OffsetDateTime.now());
        return OrderDisputeResponse.fromEntity(disputeRepository.save(dispute));
    }

    private OrderDispute findDispute(Long id) {
        if (id == null || id <= 0) throw new BusinessException("A valid dispute ID is required", 400);
        return disputeRepository.findById(id).orElseThrow(() -> new BusinessException("Dispute not found", 404));
    }

    private OrderDispute findDisputeForUpdate(Long id) {
        if (id == null || id <= 0) throw new BusinessException("A valid dispute ID is required", 400);
        return disputeRepository.findByIdForUpdate(id).orElseThrow(() -> new BusinessException("Dispute not found", 404));
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new BusinessException("Page cannot be negative", 400);
        if (size < 1 || size > 100) throw new BusinessException("Page size must be between 1 and 100", 400);
    }
}
