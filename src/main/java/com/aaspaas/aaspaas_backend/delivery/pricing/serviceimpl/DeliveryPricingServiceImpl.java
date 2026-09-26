package com.aaspaas.aaspaas_backend.delivery.pricing.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.pricing.dto.DeliveryPriceResponse;
import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import com.aaspaas.aaspaas_backend.delivery.pricing.repository.DeliveryPricingRuleRepository;
import com.aaspaas.aaspaas_backend.delivery.pricing.service.DeliveryPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryPricingServiceImpl
        implements DeliveryPricingService {

    private static final int MONEY_SCALE = 2;

    private final DeliveryPricingRuleRepository pricingRuleRepository;

    @Override
    public DeliveryPriceResponse calculateSuggestedPrice(
            String deliveryMode,
            BigDecimal roadDistanceKm
    ) {

        validateDistance(roadDistanceKm);

        DeliveryPricingRule rule =
                getActiveRule(deliveryMode);

        BigDecimal distanceCharge =
                rule.getPerKmFee()
                        .multiply(roadDistanceKm);

        BigDecimal calculatedFee =
                rule.getBaseFee()
                        .add(distanceCharge)
                        .add(rule.getFixedPlatformFee());

        BigDecimal finalFee =
                applyMinimumAndMaximum(
                        calculatedFee,
                        rule
                );

        BigDecimal estimatedCommission =
                calculateCommission(
                        finalFee,
                        rule.getCommissionPercentage(),
                        rule.getFixedPlatformFee()
                );

        BigDecimal estimatedPartnerEarning =
                finalFee.subtract(estimatedCommission)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        return DeliveryPriceResponse.builder()
                .roadDistanceKm(
                        roadDistanceKm.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .suggestedDeliveryFee(finalFee)
                .minimumDeliveryFee(
                        rule.getMinimumDeliveryFee()
                )
                .maximumDeliveryFee(
                        rule.getMaximumDeliveryFee()
                )
                .commissionPercentage(
                        rule.getCommissionPercentage()
                )
                .estimatedPlatformCommission(
                        estimatedCommission
                )
                .estimatedPartnerEarning(
                        estimatedPartnerEarning
                )
                .currency(rule.getCurrency())
                .pricingRule(rule.getRuleName())
                .build();
    }

    @Override
    public BigDecimal calculatePlatformCommission(
            BigDecimal deliveryFee,
            String deliveryMode
    ) {

        validateMoney(deliveryFee);

        DeliveryPricingRule rule =
                getActiveRule(deliveryMode);

        return calculateCommission(
                deliveryFee,
                rule.getCommissionPercentage(),
                rule.getFixedPlatformFee()
        );
    }

    private DeliveryPricingRule getActiveRule(
            String deliveryMode
    ) {

        if (deliveryMode == null
                || deliveryMode.isBlank()) {

            throw new BusinessException(
                    "Delivery mode is required",
                    400
            );
        }

        return pricingRuleRepository
                .findActiveRule(
                        deliveryMode,
                        OffsetDateTime.now()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "No active pricing rule found for delivery mode: "
                                        + deliveryMode,
                                404
                        )
                );
    }

    private BigDecimal applyMinimumAndMaximum(
            BigDecimal amount,
            DeliveryPricingRule rule
    ) {

        BigDecimal result = amount;

        if (result.compareTo(
                rule.getMinimumDeliveryFee()
        ) < 0) {

            result = rule.getMinimumDeliveryFee();
        }

        if (rule.getMaximumDeliveryFee() != null
                && result.compareTo(
                rule.getMaximumDeliveryFee()
        ) > 0) {

            result = rule.getMaximumDeliveryFee();
        }

        return result.setScale(
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateCommission(
            BigDecimal deliveryFee,
            BigDecimal percentage,
            BigDecimal fixedFee
    ) {

        BigDecimal percentageCommission =
                deliveryFee
                        .multiply(percentage)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        return percentageCommission
                .add(fixedFee)
                .setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private void validateDistance(
            BigDecimal distance
    ) {

        if (distance == null
                || distance.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "Road distance must be greater than or equal to zero",
                    400
            );
        }

        if (distance.compareTo(
                BigDecimal.valueOf(1000)
        ) > 0) {

            throw new BusinessException(
                    "Road distance exceeds supported limit",
                    400
            );
        }
    }

    private void validateMoney(
            BigDecimal amount
    ) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "Delivery fee must be greater than or equal to zero",
                    400
            );
        }
    }
}