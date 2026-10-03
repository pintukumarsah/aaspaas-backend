package com.aaspaas.aaspaas_backend.settlement.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;

import com.aaspaas.aaspaas_backend.commission.entity.CommissionEntry;
import com.aaspaas.aaspaas_backend.commission.enums.CommissionStatus;
import com.aaspaas.aaspaas_backend.commission.repository.CommissionEntryRepository;

import com.aaspaas.aaspaas_backend.delivery.entity.PartnerPayout;
import com.aaspaas.aaspaas_backend.delivery.enums.PartnerPayoutStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.PartnerPayoutRepository;
import com.aaspaas.aaspaas_backend.settlement.repository.PartnerPayoutBatchItemRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;

import com.aaspaas.aaspaas_backend.settlement.dto.PartnerPayoutBatchResponse;
import com.aaspaas.aaspaas_backend.settlement.dto.SellerSettlementResponse;

import com.aaspaas.aaspaas_backend.settlement.entity.PartnerPayoutBatch;
import com.aaspaas.aaspaas_backend.settlement.entity.PartnerPayoutBatchItem;
import com.aaspaas.aaspaas_backend.settlement.entity.SellerSettlement;
import com.aaspaas.aaspaas_backend.settlement.entity.SellerSettlementItem;

import com.aaspaas.aaspaas_backend.settlement.enums.SettlementStatus;
import com.aaspaas.aaspaas_backend.settlement.enums.SettlementType;

import com.aaspaas.aaspaas_backend.settlement.repository.PartnerPayoutBatchItemRepository;
import com.aaspaas.aaspaas_backend.settlement.repository.PartnerPayoutBatchRepository;
import com.aaspaas.aaspaas_backend.settlement.repository.SellerSettlementItemRepository;
import com.aaspaas.aaspaas_backend.settlement.repository.SellerSettlementRepository;
import com.aaspaas.aaspaas_backend.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl
        implements SettlementService {

    private final BusinessRepository businessRepository;

    private final CommissionEntryRepository
            commissionEntryRepository;

    private final SellerSettlementRepository
            sellerSettlementRepository;

    private final SellerSettlementItemRepository
            sellerSettlementItemRepository;

    private final PartnerPayoutRepository
            partnerPayoutRepository;

    private final PartnerPayoutBatchRepository
            partnerPayoutBatchRepository;

    private final PartnerPayoutBatchItemRepository
            partnerPayoutBatchItemRepository;

    @Override
    @Transactional
    public SellerSettlementResponse createSellerSettlement(
            Long businessId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {

        Business business =
                businessRepository
                        .findById(businessId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Business not found",
                                        404
                                )
                        );

        validatePeriod(
                periodStart,
                periodEnd
        );

        if (sellerSettlementRepository
                .existsByBusinessIdAndPeriodStartAndPeriodEnd(
                        businessId,
                        periodStart,
                        periodEnd
                )) {

            return sellerSettlementRepository
                    .findByBusinessIdOrderByCreatedAtDesc(
                            businessId
                    )
                    .stream()
                    .filter(settlement ->
                            settlement.getPeriodStart()
                                    .equals(periodStart)
                                    &&
                            settlement.getPeriodEnd()
                                    .equals(periodEnd)
                    )
                    .findFirst()
                    .map(this::toSellerResponse)
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Settlement already exists",
                                    409
                            )
                    );
        }

        List<CommissionEntry> entries =
                commissionEntryRepository
                        .findBusinessEntriesForPeriod(
                                businessId,
                                CommissionStatus.ACTIVE,
                                periodStart,
                                periodEnd
                        );

        if (entries.isEmpty()) {

            throw new BusinessException(
                    "No eligible commission entries found for settlement",
                    404
            );
        }

        BigDecimal grossSales =
                BigDecimal.ZERO;

        BigDecimal refunds =
                BigDecimal.ZERO;

        BigDecimal platformCommission =
                BigDecimal.ZERO;

        for (CommissionEntry entry : entries) {

            grossSales =
                    grossSales.add(
                            safe(entry.getGrossAmount())
                    );

            platformCommission =
                    platformCommission.add(
                            safe(entry.getCommissionAmount())
                    );
        }

        /*
         * Seller net:
         *
         * Gross sales
         * - platform commission
         * - refunds
         */
        BigDecimal netAmount =
                grossSales
                        .subtract(platformCommission)
                        .subtract(refunds)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (netAmount.signum() < 0) {

            netAmount =
                    BigDecimal.ZERO;
        }

        SellerSettlement settlement =
                new SellerSettlement();

        settlement.setSettlementNumber(
                generateSettlementNumber()
        );

        settlement.setBusiness(
                business
        );

        settlement.setPeriodStart(
                periodStart
        );

        settlement.setPeriodEnd(
                periodEnd
        );

        settlement.setGrossSales(
                grossSales
        );

        settlement.setRefundAmount(
                refunds
        );

        settlement.setPlatformCommission(
                platformCommission
        );

        settlement.setAdjustmentAmount(
                BigDecimal.ZERO
        );

        settlement.setNetAmount(
                netAmount
        );

        settlement.setSettlementType(
                SettlementType.REGULAR
        );

        settlement.setStatus(
                SettlementStatus.CREATED
        );

        SellerSettlement saved =
                sellerSettlementRepository.save(
                        settlement
                );

        /*
         * Snapshot every commission entry
         * into settlement items.
         */
        for (CommissionEntry entry : entries) {

            SellerSettlementItem item =
                    new SellerSettlementItem();

            item.setSettlement(
                    saved
            );

            item.setOrder(
                    entry.getOrder()
            );

            item.setGrossAmount(
                    safe(entry.getGrossAmount())
            );

            item.setRefundAmount(
                    BigDecimal.ZERO
            );

            item.setCommissionAmount(
                    safe(entry.getCommissionAmount())
            );

            item.setAdjustmentAmount(
                    BigDecimal.ZERO
            );

            item.setNetAmount(
                    safe(
                            entry.getGrossAmount()
                                    .subtract(
                                            entry.getCommissionAmount()
                                    )
                    )
            );

            sellerSettlementItemRepository.save(
                    item
            );
        }

        return toSellerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerSettlementResponse getSellerSettlement(
            Long settlementId
    ) {

        SellerSettlement settlement =
                sellerSettlementRepository
                        .findById(
                                settlementId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Seller settlement not found",
                                        404
                                )
                        );

        return toSellerResponse(
                settlement
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerSettlementResponse>
    getBusinessSettlements(
            Long businessId
    ) {

        if (!businessRepository
                .existsById(businessId)) {

            throw new BusinessException(
                    "Business not found",
                    404
            );
        }

        return sellerSettlementRepository
                .findByBusinessIdOrderByCreatedAtDesc(
                        businessId
                )
                .stream()
                .map(this::toSellerResponse)
                .toList();
    }

    @Override
    @Transactional
    public SellerSettlementResponse processSellerSettlement(
            Long settlementId
    ) {

        SellerSettlement settlement =
                sellerSettlementRepository
                        .findByIdForUpdate(
                                settlementId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Seller settlement not found",
                                        404
                                )
                        );

        if (settlement.getStatus()
                == SettlementStatus.PROCESSED) {

            return toSellerResponse(
                    settlement
            );
        }

        if (settlement.getStatus()
                == SettlementStatus.CANCELLED) {

            throw new BusinessException(
                    "Cancelled settlement cannot be processed",
                    409
            );
        }

        settlement.setStatus(
                SettlementStatus.PROCESSING
        );

        sellerSettlementRepository.save(
                settlement
        );

        /*
         * Actual bank/UPI payout integration will be
         * connected in the future payout-gateway part.
         *
         * For now this state means:
         * financial settlement has been approved
         * internally.
         */
        settlement.setStatus(
                SettlementStatus.PROCESSED
        );

        settlement.setProcessedAt(
                OffsetDateTime.now()
        );

        sellerSettlementRepository.save(
                settlement
        );

        return toSellerResponse(
                settlement
        );
    }

    @Override
    @Transactional
    public PartnerPayoutBatchResponse
    createPartnerPayoutBatch() {

        List<PartnerPayout> eligiblePayouts =
                partnerPayoutRepository
                        .findByStatusOrderByCreatedAtAsc(
                                PartnerPayoutStatus.ELIGIBLE
                        );

        if (eligiblePayouts.isEmpty()) {

            throw new BusinessException(
                    "No eligible partner payouts found",
                    404
            );
        }

        PartnerPayoutBatch batch =
                new PartnerPayoutBatch();

        batch.setBatchNumber(
                generateBatchNumber()
        );

        batch.setStatus(
                SettlementStatus.CREATED
        );

        batch.setTotalGrossAmount(
                BigDecimal.ZERO
        );

        batch.setTotalPlatformFee(
                BigDecimal.ZERO
        );

        batch.setTotalNetAmount(
                BigDecimal.ZERO
        );

        batch.setPayoutCount(0);

        PartnerPayoutBatch savedBatch =
                partnerPayoutBatchRepository.save(
                        batch
                );

        BigDecimal gross =
                BigDecimal.ZERO;

        BigDecimal platformFee =
                BigDecimal.ZERO;

        BigDecimal net =
                BigDecimal.ZERO;

        int count = 0;

        for (PartnerPayout payout : eligiblePayouts) {

            if (partnerPayoutBatchItemRepository
                    .existsByPartnerPayoutId(
                            payout.getId()
                    )) {

                continue;
            }

            PartnerPayoutBatchItem item =
                    new PartnerPayoutBatchItem();

            item.setBatch(
                    savedBatch
            );

            item.setPartnerPayout(
                    payout
            );

            item.setAmount(
                    safe(payout.getNetAmount())
            );

            partnerPayoutBatchItemRepository.save(
                    item
            );

            payout.setStatus(
                    PartnerPayoutStatus.PROCESSING
            );

            partnerPayoutRepository.save(
                    payout
            );

            gross =
                    gross.add(
                            safe(
                                    payout.getGrossAmount()
                            )
                    );

            platformFee =
                    platformFee.add(
                            safe(
                                    payout.getPlatformFee()
                            )
                    );

            net =
                    net.add(
                            safe(
                                    payout.getNetAmount()
                            )
                    );

            count++;
        }

        if (count == 0) {

            savedBatch.setStatus(
                    SettlementStatus.CANCELLED
            );

            partnerPayoutBatchRepository.save(
                    savedBatch
            );

            throw new BusinessException(
                    "No new eligible payouts available",
                    409
            );
        }

        savedBatch.setTotalGrossAmount(
                gross
        );

        savedBatch.setTotalPlatformFee(
                platformFee
        );

        savedBatch.setTotalNetAmount(
                net
        );

        savedBatch.setPayoutCount(
                count
        );

        savedBatch.setStatus(
                SettlementStatus.CREATED
        );

        partnerPayoutBatchRepository.save(
                savedBatch
        );

        return toBatchResponse(
                savedBatch
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerPayoutBatchResponse
    getPartnerPayoutBatch(
            Long batchId
    ) {

        PartnerPayoutBatch batch =
                partnerPayoutBatchRepository
                        .findById(
                                batchId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Partner payout batch not found",
                                        404
                                )
                        );

        return toBatchResponse(
                batch
        );
    }

    @Override
    @Transactional
    public PartnerPayoutBatchResponse
    processPartnerPayoutBatch(
            Long batchId
    ) {

        PartnerPayoutBatch batch =
                partnerPayoutBatchRepository
                        .findByIdForUpdate(
                                batchId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Partner payout batch not found",
                                        404
                                )
                        );

        if (batch.getStatus()
                == SettlementStatus.PROCESSED) {

            return toBatchResponse(
                    batch
            );
        }

        if (batch.getStatus()
                == SettlementStatus.CANCELLED) {

            throw new BusinessException(
                    "Cancelled payout batch cannot be processed",
                    409
            );
        }

        batch.setStatus(
                SettlementStatus.PROCESSING
        );

        partnerPayoutBatchRepository.save(
                batch
        );

        /*
         * Actual bank payout gateway is deliberately
         * not faked here.
         *
         * Until the real payout provider is integrated,
         * keep batch PROCESSING instead of falsely
         * marking money as PAID.
         */
        return toBatchResponse(
                batch
        );
    }

    private void validatePeriod(
            OffsetDateTime start,
            OffsetDateTime end
    ) {

        if (start == null
                || end == null) {

            throw new BusinessException(
                    "Settlement period is required",
                    400
            );
        }

        if (!end.isAfter(start)) {

            throw new BusinessException(
                    "Settlement end must be after start",
                    400
            );
        }
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

    private String generateSettlementNumber() {

        return "SET-"
                + OffsetDateTime.now()
                .format(
                        DateTimeFormatter
                                .ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                );
    }

    private String generateBatchNumber() {

        return "PB-"
                + OffsetDateTime.now()
                .format(
                        DateTimeFormatter
                                .ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                );
    }

    private SellerSettlementResponse
    toSellerResponse(
            SellerSettlement settlement
    ) {

        return SellerSettlementResponse.builder()
                .id(settlement.getId())
                .settlementNumber(
                        settlement.getSettlementNumber()
                )
                .businessId(
                        settlement.getBusiness().getId()
                )
                .periodStart(
                        settlement.getPeriodStart()
                )
                .periodEnd(
                        settlement.getPeriodEnd()
                )
                .grossSales(
                        settlement.getGrossSales()
                )
                .refundAmount(
                        settlement.getRefundAmount()
                )
                .platformCommission(
                        settlement.getPlatformCommission()
                )
                .adjustmentAmount(
                        settlement.getAdjustmentAmount()
                )
                .netAmount(
                        settlement.getNetAmount()
                )
                .status(
                        settlement.getStatus()
                )
                .settlementType(
                        settlement.getSettlementType()
                )
                .processedAt(
                        settlement.getProcessedAt()
                )
                .createdAt(
                        settlement.getCreatedAt()
                )
                .build();
    }

    private PartnerPayoutBatchResponse
    toBatchResponse(
            PartnerPayoutBatch batch
    ) {

        return PartnerPayoutBatchResponse.builder()
                .id(batch.getId())
                .batchNumber(
                        batch.getBatchNumber()
                )
                .totalGrossAmount(
                        batch.getTotalGrossAmount()
                )
                .totalPlatformFee(
                        batch.getTotalPlatformFee()
                )
                .totalNetAmount(
                        batch.getTotalNetAmount()
                )
                .payoutCount(
                        batch.getPayoutCount()
                )
                .status(
                        batch.getStatus()
                )
                .processedAt(
                        batch.getProcessedAt()
                )
                .createdAt(
                        batch.getCreatedAt()
                )
                .build();
    }
}