package com.aaspaas.aaspaas_backend.admin.serviceimpl;

import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleRequest;
import com.aaspaas.aaspaas_backend.admin.dto.DeliveryPricingRuleResponse;
import com.aaspaas.aaspaas_backend.admin.service.DeliveryPricingAdminService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.pricing.entity.DeliveryPricingRule;
import com.aaspaas.aaspaas_backend.delivery.pricing.repository.DeliveryPricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeliveryPricingAdminServiceImpl implements DeliveryPricingAdminService {
    private static final Set<String> SUPPORTED_MODES = Set.of("DIRECT", "ROUTE_MATCH", "SELF_PICKUP");
    private final DeliveryPricingRuleRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryPricingRuleResponse> listRules(Boolean active, String deliveryMode, int page, int size) {
        validatePage(page, size);
        String mode = normalizeModeOrNull(deliveryMode);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DeliveryPricingRule> result;
        if (active != null && mode != null) result = repository.findByActiveAndDeliveryMode(active, mode, pageable);
        else if (active != null) result = repository.findByActive(active, pageable);
        else if (mode != null) result = repository.findByDeliveryMode(mode, pageable);
        else result = repository.findAll(pageable);
        return result.map(DeliveryPricingRuleResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryPricingRuleResponse getRule(Long id) {
        return DeliveryPricingRuleResponse.fromEntity(findRule(id));
    }

    @Override
    @Transactional
    public DeliveryPricingRuleResponse createRule(DeliveryPricingRuleRequest request) {
        validateRequest(request);
        DeliveryPricingRule rule = new DeliveryPricingRule();
        apply(rule, request);
        if (rule.isActive()) ensureNoActiveOverlap(rule, null);
        return DeliveryPricingRuleResponse.fromEntity(repository.save(rule));
    }

    @Override
    @Transactional
    public DeliveryPricingRuleResponse updateRule(Long id, DeliveryPricingRuleRequest request) {
        validateRequest(request);
        DeliveryPricingRule rule = findRule(id);
        apply(rule, request);
        if (rule.isActive()) ensureNoActiveOverlap(rule, id);
        return DeliveryPricingRuleResponse.fromEntity(repository.save(rule));
    }

    @Override
    @Transactional
    public DeliveryPricingRuleResponse setActive(Long id, boolean active) {
        DeliveryPricingRule rule = findRule(id);
        if (rule.isActive() == active) return DeliveryPricingRuleResponse.fromEntity(rule);
        rule.setActive(active);
        if (active) {
            validateDateRange(rule.getEffectiveFrom(), rule.getEffectiveTo());
            ensureNoActiveOverlap(rule, id);
        }
        return DeliveryPricingRuleResponse.fromEntity(repository.save(rule));
    }

    private void apply(DeliveryPricingRule rule, DeliveryPricingRuleRequest request) {
        String mode = normalizeModeOrNull(request.getDeliveryMode());
        if (mode == null) throw new BusinessException("Unsupported delivery mode. Use DIRECT, ROUTE_MATCH, or SELF_PICKUP", 400);
        rule.setRuleName(request.getRuleName().trim());
        rule.setDeliveryMode(mode);
        rule.setBaseFee(request.getBaseFee());
        rule.setPerKmFee(request.getPerKmFee());
        rule.setMinimumDeliveryFee(request.getMinimumDeliveryFee());
        rule.setMaximumDeliveryFee(request.getMaximumDeliveryFee());
        rule.setCommissionPercentage(request.getCommissionPercentage());
        rule.setFixedPlatformFee(request.getFixedPlatformFee());
        rule.setCurrency(request.getCurrency().trim().toUpperCase(Locale.ROOT));
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        rule.setActive(request.getActive() == null || request.getActive());
    }

    private void validateRequest(DeliveryPricingRuleRequest request) {
        if (request == null) throw new BusinessException("Request body is required", 400);
        validateDateRange(request.getEffectiveFrom(), request.getEffectiveTo());
        if (request.getMinimumDeliveryFee() != null && request.getMaximumDeliveryFee() != null
                && request.getMaximumDeliveryFee().compareTo(request.getMinimumDeliveryFee()) < 0) {
            throw new BusinessException("Maximum delivery fee must be greater than or equal to minimum delivery fee", 400);
        }
        if (request.getBaseFee() != null && request.getPerKmFee() != null && request.getFixedPlatformFee() != null
                && request.getMinimumDeliveryFee() != null && request.getMaximumDeliveryFee() != null) {
            // Prevent a minimum fee above the configured cap, which would make the pricing rule contradictory.
            if (request.getMaximumDeliveryFee().compareTo(request.getMinimumDeliveryFee()) < 0) {
                throw new BusinessException("Maximum delivery fee cannot be lower than minimum delivery fee", 400);
            }
        }
    }

    private void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from == null) throw new BusinessException("Effective-from date/time is required", 400);
        if (to != null && !to.isAfter(from)) throw new BusinessException("Effective-to must be after effective-from", 400);
    }

    private void ensureNoActiveOverlap(DeliveryPricingRule candidate, Long excludeId) {
        if (repository.existsOverlappingActiveRule(candidate.getDeliveryMode(), candidate.getEffectiveFrom(),
                candidate.getEffectiveTo(), excludeId)) {
            throw new BusinessException("An active pricing rule already overlaps this delivery mode and effective period", 409);
        }
    }

    private DeliveryPricingRule findRule(Long id) {
        if (id == null || id <= 0) throw new BusinessException("A valid pricing rule ID is required", 400);
        return repository.findById(id).orElseThrow(() -> new BusinessException("Delivery pricing rule not found", 404));
    }

    private String normalizeModeOrNull(String mode) {
        if (mode == null || mode.isBlank()) return null;
        String normalized = mode.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_MODES.contains(normalized)) throw new BusinessException("Unsupported delivery mode. Use DIRECT, ROUTE_MATCH, or SELF_PICKUP", 400);
        return normalized;
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new BusinessException("Page cannot be negative", 400);
        if (size < 1 || size > 100) throw new BusinessException("Page size must be between 1 and 100", 400);
    }
}
