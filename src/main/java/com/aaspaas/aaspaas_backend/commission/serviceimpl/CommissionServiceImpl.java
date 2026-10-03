package com.aaspaas.aaspaas_backend.commission.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;

import com.aaspaas.aaspaas_backend.commission.dto.CommissionResponse;
import com.aaspaas.aaspaas_backend.commission.dto.CreateCommissionRequest;
import com.aaspaas.aaspaas_backend.commission.entity.CommissionEntry;
import com.aaspaas.aaspaas_backend.commission.enums.CommissionStatus;
import com.aaspaas.aaspaas_backend.commission.repository.CommissionEntryRepository;
import com.aaspaas.aaspaas_backend.commission.service.CommissionService;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;

import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl
        implements CommissionService {

    private final CommissionEntryRepository
            commissionEntryRepository;

    private final OrderRepository
            orderRepository;

    private final BusinessRepository
            businessRepository;

    private final DeliveryAssignmentRepository
            deliveryAssignmentRepository;

    private final DeliveryPartnerRepository
            deliveryPartnerRepository;

    @Override
    @Transactional
    public CommissionResponse createCommission(
            CreateCommissionRequest request
    ) {

        if (commissionEntryRepository
                .existsByOrderIdAndCommissionType(
                        request.getOrderId(),
                        request.getCommissionType()
                )) {

            CommissionEntry existing =
                    commissionEntryRepository
                            .findByOrderIdOrderByCreatedAtAsc(
                                    request.getOrderId()
                            )
                            .stream()
                            .filter(entry ->
                                    request.getCommissionType()
                                            .equalsIgnoreCase(
                                                    entry.getCommissionType()
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Commission already exists",
                                            409
                                    )
                            );

            return toResponse(existing);
        }

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Order not found",
                                        404
                                )
                        );

        if ("CANCELLED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Cannot create commission for cancelled order",
                    409
            );
        }

        BigDecimal grossAmount =
                safe(request.getGrossAmount());

        BigDecimal commissionAmount =
                safe(request.getCommissionAmount());

        if (commissionAmount
                .compareTo(grossAmount) > 0) {

            throw new BusinessException(
                    "Commission cannot exceed gross amount",
                    400
            );
        }

        Business business = null;

        if (request.getBusinessId() != null) {

            business =
                    businessRepository
                            .findById(
                                    request.getBusinessId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Business not found",
                                            404
                                    )
                            );

            if (!business.getId()
                    .equals(
                            order.getBusiness().getId()
                    )) {

                throw new BusinessException(
                        "Business does not belong to this order",
                        400
                );
            }
        }

        DeliveryAssignment assignment = null;

        if (request.getDeliveryAssignmentId()
                != null) {

            assignment =
                    deliveryAssignmentRepository
                            .findById(
                                    request
                                            .getDeliveryAssignmentId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Delivery assignment not found",
                                            404
                                    )
                            );

            if (!assignment
                    .getDeliveryRequest()
                    .getOrder()
                    .getId()
                    .equals(order.getId())) {

                throw new BusinessException(
                        "Delivery assignment does not belong to order",
                        400
                );
            }
        }

        DeliveryPartner partner = null;

        if (request.getPartnerId() != null) {

            partner =
                    deliveryPartnerRepository
                            .findById(
                                    request.getPartnerId()
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Delivery partner not found",
                                            404
                                    )
                            );

            if (assignment != null
                    && !partner.getId()
                    .equals(
                            assignment.getPartner().getId()
                    )) {

                throw new BusinessException(
                        "Partner does not belong to delivery assignment",
                        400
                );
            }
        }

        CommissionEntry entry =
                new CommissionEntry();

        entry.setOrder(order);
        entry.setBusiness(business);
        entry.setDeliveryAssignment(assignment);
        entry.setPartner(partner);

        entry.setCommissionType(
                request.getCommissionType()
                        .trim()
                        .toUpperCase()
        );

        entry.setGrossAmount(
                grossAmount
        );

        entry.setCommissionRate(
                request.getCommissionRate()
        );

        entry.setCommissionAmount(
                commissionAmount
        );

        entry.setDescription(
                request.getDescription()
        );

        entry.setStatus(
                CommissionStatus.ACTIVE
        );

        CommissionEntry saved =
                commissionEntryRepository.save(
                        entry
                );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionResponse getCommission(
            Long commissionId
    ) {

        CommissionEntry entry =
                commissionEntryRepository
                        .findById(
                                commissionId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Commission not found",
                                        404
                                )
                        );

        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getOrderCommissions(
            Long orderId
    ) {

        if (!orderRepository.existsById(orderId)) {

            throw new BusinessException(
                    "Order not found",
                    404
            );
        }

        return commissionEntryRepository
                .findByOrderIdOrderByCreatedAtAsc(
                        orderId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getBusinessCommissions(
            Long businessId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    ) {

        if (!businessRepository
                .existsById(businessId)) {

            throw new BusinessException(
                    "Business not found",
                    404
            );
        }

        if (startDate == null
                || endDate == null
                || !endDate.isAfter(startDate)) {

            throw new BusinessException(
                    "Invalid settlement period",
                    400
            );
        }

        return commissionEntryRepository
                .findBusinessEntriesForPeriod(
                        businessId,
                        CommissionStatus.ACTIVE,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommissionResponse reverseCommission(
            Long commissionId,
            String reason
    ) {

        CommissionEntry original =
                commissionEntryRepository
                        .findByIdForUpdate(
                                commissionId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Commission not found",
                                        404
                                )
                        );

        if (original.getStatus()
                == CommissionStatus.REVERSED) {

            return toResponse(original);
        }

        if (original.getStatus()
                != CommissionStatus.ACTIVE) {

            throw new BusinessException(
                    "Only active commission can be reversed",
                    409
            );
        }

        original.setStatus(
                CommissionStatus.REVERSED
        );

        commissionEntryRepository.save(
                original
        );

        CommissionEntry reversal =
                new CommissionEntry();

        reversal.setOrder(
                original.getOrder()
        );

        reversal.setBusiness(
                original.getBusiness()
        );

        reversal.setDeliveryAssignment(
                original.getDeliveryAssignment()
        );

        reversal.setPartner(
                original.getPartner()
        );

        reversal.setCommissionType(
                original.getCommissionType()
                        + "_REVERSAL"
        );

        reversal.setGrossAmount(
                original.getGrossAmount()
        );

        reversal.setCommissionRate(
                original.getCommissionRate()
        );

        reversal.setCommissionAmount(
                original.getCommissionAmount()
        );

        reversal.setStatus(
                CommissionStatus.REVERSED
        );

        reversal.setReversalOf(
                original
        );

        reversal.setDescription(
                reason
        );

        CommissionEntry saved =
                commissionEntryRepository.save(
                        reversal
                );

        return toResponse(saved);
    }

    private BigDecimal safe(
            BigDecimal value
    ) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private CommissionResponse toResponse(
            CommissionEntry entry
    ) {

        return CommissionResponse.builder()
                .id(entry.getId())
                .orderId(
                        entry.getOrder()
                                .getId()
                )
                .deliveryAssignmentId(
                        entry.getDeliveryAssignment() == null
                                ? null
                                : entry.getDeliveryAssignment()
                                        .getId()
                )
                .businessId(
                        entry.getBusiness() == null
                                ? null
                                : entry.getBusiness()
                                        .getId()
                )
                .partnerId(
                        entry.getPartner() == null
                                ? null
                                : entry.getPartner()
                                        .getId()
                )
                .commissionType(
                        entry.getCommissionType()
                )
                .grossAmount(
                        entry.getGrossAmount()
                )
                .commissionRate(
                        entry.getCommissionRate()
                )
                .commissionAmount(
                        entry.getCommissionAmount()
                )
                .status(
                        entry.getStatus()
                )
                .description(
                        entry.getDescription()
                )
                .createdAt(
                        entry.getCreatedAt()
                )
                .build();
    }
}