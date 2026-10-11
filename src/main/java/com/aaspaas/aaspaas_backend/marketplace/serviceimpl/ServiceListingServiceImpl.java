package com.aaspaas.aaspaas_backend.marketplace.serviceimpl;

import com.aaspaas.aaspaas_backend.category.entity.Category;
import com.aaspaas.aaspaas_backend.category.repository.CategoryRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.marketplace.dto.ListingRequest;
import com.aaspaas.aaspaas_backend.marketplace.dto.ListingResponse;
import com.aaspaas.aaspaas_backend.marketplace.entity.ServiceListing;
import com.aaspaas.aaspaas_backend.marketplace.enums.ListingStatus;
import com.aaspaas.aaspaas_backend.marketplace.repository.ServiceListingRepository;
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceListingService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ServiceListingServiceImpl implements ServiceListingService {

    private final ServiceListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Value("${marketplace.max-listings-per-provider:20}")
    private long maxListingsPerProvider;

    // ------------------------------------------------------------------ provider

    @Override
    @Transactional
    public ListingResponse create(ListingRequest request, String phone) {
        User provider = provider(phone);
        if (listingRepository.countByProviderId(provider.getId()) >= maxListingsPerProvider) {
            throw new BusinessException(
                    "Maximum " + maxListingsPerProvider + " listings allowed", 409);
        }
        ServiceListing l = new ServiceListing();
        l.setProvider(provider);
        apply(l, request);
        l.setStatus(ListingStatus.PENDING_APPROVAL);
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional
    public ListingResponse update(Long id, ListingRequest request, String phone) {
        ServiceListing l = ownedLocked(id, provider(phone));
        if (l.getStatus() == ListingStatus.SUSPENDED) {
            throw new BusinessException("A suspended listing cannot be edited", 409);
        }
        apply(l, request);
        // A content change on a live listing must be re-approved.
        if (l.getStatus() == ListingStatus.ACTIVE) {
            l.setStatus(ListingStatus.PENDING_APPROVAL);
        }
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional
    public ListingResponse pause(Long id, String phone) {
        ServiceListing l = ownedLocked(id, provider(phone));
        if (l.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException("Only an ACTIVE listing can be paused", 409);
        }
        l.setStatus(ListingStatus.PAUSED);
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional
    public ListingResponse resume(Long id, String phone) {
        ServiceListing l = ownedLocked(id, provider(phone));
        if (l.getStatus() != ListingStatus.PAUSED) {
            throw new BusinessException("Only a PAUSED listing can be resumed", 409);
        }
        l.setStatus(ListingStatus.ACTIVE);
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponse> myListings(String phone, int page, int size) {
        User provider = provider(phone);
        return listingRepository
                .findByProviderIdOrderByCreatedAtDesc(provider.getId(), pageable(page, size))
                .map(this::toResponse);
    }

    // ------------------------------------------------------------------ public

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponse> search(
            Long categoryId, String city, String query, int page, int size) {

        Specification<ServiceListing> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("status"), ListingStatus.ACTIVE));
            if (categoryId != null) {
                p.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (city != null && !city.isBlank()) {
                p.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase(Locale.ROOT)));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%";
                p.add(cb.like(cb.lower(root.get("title")), like, '\\'));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return listingRepository.findAll(spec, pageable(page, size)).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponse getPublic(Long id) {
        ServiceListing l = listingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Service not found", 404));
        if (l.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException("Service not found", 404);
        }
        return toResponse(l);
    }

    // ------------------------------------------------------------------ admin

    @Override
    @Transactional(readOnly = true)
    public Page<ListingResponse> adminList(ListingStatus status, int page, int size) {
        Specification<ServiceListing> spec = (root, cq, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
        return listingRepository.findAll(spec, pageable(page, size)).map(this::toResponse);
    }

    @Override
    @Transactional
    public ListingResponse approve(Long id) {
        ServiceListing l = locked(id);
        if (l.getStatus() != ListingStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only a PENDING_APPROVAL listing can be approved", 409);
        }
        l.setStatus(ListingStatus.ACTIVE);
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional
    public ListingResponse suspend(Long id) {
        ServiceListing l = locked(id);
        if (l.getStatus() == ListingStatus.SUSPENDED) {
            return toResponse(l);
        }
        l.setStatus(ListingStatus.SUSPENDED);
        return toResponse(listingRepository.save(l));
    }

    @Override
    @Transactional
    public ListingResponse reactivate(Long id) {
        ServiceListing l = locked(id);
        if (l.getStatus() != ListingStatus.SUSPENDED) {
            throw new BusinessException("Only a SUSPENDED listing can be reactivated", 409);
        }
        l.setStatus(ListingStatus.ACTIVE);
        return toResponse(listingRepository.save(l));
    }

    // ------------------------------------------------------------------ helpers

    private void apply(ServiceListing l, ListingRequest r) {
        Category category = categoryRepository.findById(r.getCategoryId())
                .orElseThrow(() -> new BusinessException("Category not found", 404));
        if (!"SERVICE".equalsIgnoreCase(category.getType())
                || !"ACTIVE".equalsIgnoreCase(category.getStatus())) {
            throw new BusinessException("Category must be an ACTIVE service category", 400);
        }
        if ((r.getLatitude() == null) != (r.getLongitude() == null)) {
            throw new BusinessException("Latitude and longitude must be provided together", 400);
        }
        l.setCategory(category);
        l.setTitle(r.getTitle().trim());
        l.setDescription(r.getDescription().trim());
        l.setPricingType(r.getPricingType());
        l.setBasePrice(r.getBasePrice());
        l.setCity(r.getCity().trim());
        l.setLocality(r.getLocality() == null ? null : r.getLocality().trim());
        l.setLatitude(r.getLatitude());
        l.setLongitude(r.getLongitude());
    }

    private User provider(String phone) {
        User u = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found", 404));
        boolean ok = u.getRoles().stream()
                .anyMatch(r -> "SERVICE_PROVIDER".equalsIgnoreCase(r.getName()));
        if (!ok) {
            throw new BusinessException("Only a service provider can manage listings", 403);
        }
        return u;
    }

    private ServiceListing ownedLocked(Long id, User provider) {
        ServiceListing l = locked(id);
        if (!l.getProvider().getId().equals(provider.getId())) {
            // 404 on purpose: do not reveal that someone else's listing exists
            throw new BusinessException("Service not found", 404);
        }
        return l;
    }

    private ServiceListing locked(Long id) {
        return listingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Service not found", 404));
    }

    private PageRequest pageable(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Page cannot be negative", 400);
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("Page size must be between 1 and 100", 400);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private ListingResponse toResponse(ServiceListing l) {
        return ListingResponse.builder()
                .id(l.getId())
                .providerId(l.getProvider().getId())
                .providerName(l.getProvider().getFullName())
                .categoryId(l.getCategory().getId())
                .categoryName(l.getCategory().getName())
                .title(l.getTitle())
                .description(l.getDescription())
                .pricingType(l.getPricingType())
                .basePrice(l.getBasePrice())
                .city(l.getCity())
                .locality(l.getLocality())
                .latitude(l.getLatitude())
                .longitude(l.getLongitude())
                .status(l.getStatus())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
