package com.aaspaas.aaspaas_backend.rating.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;

import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryAssignment;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryPartner;
import com.aaspaas.aaspaas_backend.delivery.enums.DeliveryAssignmentStatus;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryAssignmentRepository;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryPartnerRepository;

import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;

import com.aaspaas.aaspaas_backend.rating.dto.CreateRatingRequest;
import com.aaspaas.aaspaas_backend.rating.dto.RatingResponse;
import com.aaspaas.aaspaas_backend.rating.dto.RatingSummaryResponse;
import com.aaspaas.aaspaas_backend.rating.entity.Rating;
import com.aaspaas.aaspaas_backend.rating.enums.RatingStatus;
import com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType;
import com.aaspaas.aaspaas_backend.rating.repository.RatingRepository;
import com.aaspaas.aaspaas_backend.rating.service.RatingService;

import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl
        implements RatingService {


    private final RatingRepository ratingRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final BusinessRepository businessRepository;

    private final DeliveryAssignmentRepository
            deliveryAssignmentRepository;

    private final DeliveryPartnerRepository
            deliveryPartnerRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @Transactional
    public RatingResponse createRating(
            CreateRatingRequest request
    ) {

        validateRequest(request);

        User reviewer =
                getLoggedInUser();

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


        // =====================================================
        // CUSTOMER OWNERSHIP
        // =====================================================

        if (!order.getCustomer()
                .getId()
                .equals(reviewer.getId())) {

            throw new BusinessException(
                    "You can rate only your own order",
                    403
            );
        }


        // =====================================================
        // ORDER MUST BE DELIVERED
        // =====================================================

        if (!"DELIVERED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Rating is allowed only after order delivery",
                    409
            );
        }


        if (request.getTargetType()
                == RatingTargetType.BUSINESS) {

            return createBusinessRating(
                    reviewer,
                    order,
                    request
            );
        }


        if (request.getTargetType()
                == RatingTargetType.DELIVERY_PARTNER) {

            return createDeliveryPartnerRating(
                    reviewer,
                    order,
                    request
            );
        }


        throw new BusinessException(
                "Unsupported rating target",
                400
        );
    }


    // =========================================================
    // BUSINESS RATING
    // =========================================================

    private RatingResponse createBusinessRating(
            User reviewer,
            Order order,
            CreateRatingRequest request
    ) {

        if (request.getBusinessId() == null) {

            throw new BusinessException(
                    "Business ID is required",
                    400
            );
        }


        Business business =
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
                .equals(order.getBusiness().getId())) {

            throw new BusinessException(
                    "Business does not belong to this order",
                    409
            );
        }


        Rating rating =
                ratingRepository
                        .findByReviewerIdAndOrderIdAndTargetBusinessId(
                                reviewer.getId(),
                                order.getId(),
                                business.getId()
                        )
                        .orElse(null);


        if (rating != null) {

            if (rating.getStatus()
                    == RatingStatus.ACTIVE) {

                throw new BusinessException(
                        "You have already rated this business for this order",
                        409
                );
            }

            rating.setRating(
                    request.getRating()
            );

            rating.setReview(
                    normalizeReview(
                            request.getReview()
                    )
            );

            rating.setStatus(
                    RatingStatus.ACTIVE
            );

            return saveRatingSafely(
                    rating
            );
        }


        rating =
                new Rating();

        rating.setReviewer(
                reviewer
        );

        rating.setOrder(
                order
        );

        rating.setTargetType(
                RatingTargetType.BUSINESS
        );

        rating.setTargetBusiness(
                business
        );

        rating.setRating(
                request.getRating()
        );

        rating.setReview(
                normalizeReview(
                        request.getReview()
                )
        );

        rating.setStatus(
                RatingStatus.ACTIVE
        );


        return saveRatingSafely(
                rating
        );
    }


    // =========================================================
    // DELIVERY PARTNER RATING
    // =========================================================

    private RatingResponse createDeliveryPartnerRating(
            User reviewer,
            Order order,
            CreateRatingRequest request
    ) {

        if (request.getDeliveryPartnerId() == null) {

            throw new BusinessException(
                    "Delivery partner ID is required",
                    400
            );
        }


        if (request.getDeliveryAssignmentId() == null) {

            throw new BusinessException(
                    "Delivery assignment ID is required",
                    400
            );
        }


        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(
                                request.getDeliveryPartnerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery partner not found",
                                        404
                                )
                        );


        DeliveryAssignment assignment =
                deliveryAssignmentRepository
                        .findById(
                                request.getDeliveryAssignmentId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery assignment not found",
                                        404
                                )
                        );


        // =====================================================
        // ASSIGNMENT MUST BELONG TO ORDER
        // =====================================================

        if (!assignment.getDeliveryRequest()
                .getOrder()
                .getId()
                .equals(order.getId())) {

            throw new BusinessException(
                    "Delivery assignment does not belong to this order",
                    409
            );
        }


        // =====================================================
        // ASSIGNMENT MUST BELONG TO PARTNER
        // =====================================================

        if (!assignment.getPartner()
                .getId()
                .equals(partner.getId())) {

            throw new BusinessException(
                    "Delivery partner does not belong to this assignment",
                    409
            );
        }


        // =====================================================
        // DELIVERY MUST BE COMPLETED
        // =====================================================

        if (assignment.getStatus()
                != DeliveryAssignmentStatus.DELIVERED) {

            throw new BusinessException(
                    "Delivery partner can be rated only after delivery",
                    409
            );
        }


        User targetUser =
                partner.getUser();


        Rating rating =
                ratingRepository
                        .findByReviewerIdAndOrderIdAndTargetUserIdAndDeliveryAssignmentId(
                                reviewer.getId(),
                                order.getId(),
                                targetUser.getId(),
                                assignment.getId()
                        )
                        .orElse(null);


        if (rating != null) {

            if (rating.getStatus()
                    == RatingStatus.ACTIVE) {

                throw new BusinessException(
                        "You have already rated this delivery partner",
                        409
                );
            }

            rating.setRating(
                    request.getRating()
            );

            rating.setReview(
                    normalizeReview(
                            request.getReview()
                    )
            );

            rating.setStatus(
                    RatingStatus.ACTIVE
            );

            RatingResponse response =
                    saveRatingSafely(
                            rating
                    );

            refreshDeliveryPartnerRating(
                    partner.getId()
            );

            return response;
        }


        rating =
                new Rating();

        rating.setReviewer(
                reviewer
        );

        rating.setOrder(
                order
        );

        rating.setTargetType(
                RatingTargetType.DELIVERY_PARTNER
        );

        rating.setTargetUser(
                targetUser
        );

        rating.setDeliveryAssignment(
                assignment
        );

        rating.setRating(
                request.getRating()
        );

        rating.setReview(
                normalizeReview(
                        request.getReview()
                )
        );

        rating.setStatus(
                RatingStatus.ACTIVE
        );


        RatingResponse response =
                saveRatingSafely(
                        rating
                );


        refreshDeliveryPartnerRating(
                partner.getId()
        );


        return response;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public RatingResponse updateRating(
            Long ratingId,
            CreateRatingRequest request
    ) {

        validateRequest(request);

        User reviewer =
                getLoggedInUser();


        Rating rating =
                ratingRepository
                        .findByIdForUpdate(
                                ratingId,
                                reviewer.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Rating not found or you are not allowed to update it",
                                        404
                                )
                        );


        if (rating.getStatus()
                == RatingStatus.DELETED) {

            throw new BusinessException(
                    "Deleted rating cannot be updated",
                    409
            );
        }


        if (!rating.getTargetType()
                .equals(request.getTargetType())) {

            throw new BusinessException(
                    "Rating target type cannot be changed",
                    409
            );
        }


        if (rating.getTargetType()
                == RatingTargetType.BUSINESS) {

            if (!rating.getTargetBusiness()
                    .getId()
                    .equals(request.getBusinessId())) {

                throw new BusinessException(
                        "Rating business cannot be changed",
                        409
                );
            }
        }


        if (rating.getTargetType()
                == RatingTargetType.DELIVERY_PARTNER) {

            if (!rating.getDeliveryAssignment()
                    .getId()
                    .equals(
                            request.getDeliveryAssignmentId()
                    )) {

                throw new BusinessException(
                        "Delivery assignment cannot be changed",
                        409
                );
            }
        }


        rating.setRating(
                request.getRating()
        );

        rating.setReview(
                normalizeReview(
                        request.getReview()
                )
        );


        RatingResponse response =
                saveRatingSafely(
                        rating
                );


        if (rating.getTargetType()
                == RatingTargetType.DELIVERY_PARTNER) {

            refreshDeliveryPartnerRating(
                    rating.getTargetUser()
                            .getId()
            );
        }


        return response;
    }


    // =========================================================
    // DELETE - SOFT DELETE
    // =========================================================

    @Override
    @Transactional
    public void deleteRating(
            Long ratingId
    ) {

        User reviewer =
                getLoggedInUser();


        Rating rating =
                ratingRepository
                        .findByIdForUpdate(
                                ratingId,
                                reviewer.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Rating not found or you are not allowed to delete it",
                                        404
                                )
                        );


        if (rating.getStatus()
                == RatingStatus.DELETED) {

            return;
        }


        Long targetUserId =
                rating.getTargetUser() != null
                        ? rating.getTargetUser().getId()
                        : null;


        rating.setStatus(
                RatingStatus.DELETED
        );


        ratingRepository.save(
                rating
        );


        if (targetUserId != null
                && rating.getTargetType()
                == RatingTargetType.DELIVERY_PARTNER) {

            refreshDeliveryPartnerRatingByUserId(
                    targetUserId
            );
        }
    }


    // =========================================================
    // GET SINGLE RATING
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getRating(
            Long ratingId
    ) {

        Rating rating =
                ratingRepository
                        .findById(
                                ratingId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Rating not found",
                                        404
                                )
                        );


        if (rating.getStatus()
                != RatingStatus.ACTIVE) {

            throw new BusinessException(
                    "Rating is not available",
                    404
            );
        }


        return mapToResponse(
                rating
        );
    }


    // =========================================================
    // BUSINESS RATINGS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<RatingResponse> getBusinessRatings(
            Long businessId,
            int page,
            int size
    ) {

        validatePage(
                page,
                size
        );


        if (!businessRepository
                .existsById(
                        businessId
                )) {

            throw new BusinessException(
                    "Business not found",
                    404
            );
        }


        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        return ratingRepository
                .findByTargetBusinessIdAndStatusOrderByCreatedAtDesc(
                        businessId,
                        RatingStatus.ACTIVE,
                        pageable
                )
                .map(
                        this::mapToResponse
                );
    }


    // =========================================================
    // DELIVERY PARTNER RATINGS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<RatingResponse> getDeliveryPartnerRatings(
            Long deliveryPartnerId,
            int page,
            int size
    ) {

        validatePage(
                page,
                size
        );


        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(
                                deliveryPartnerId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery partner not found",
                                        404
                                )
                        );


        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        return ratingRepository
                .findByTargetUserIdAndTargetTypeAndStatusOrderByCreatedAtDesc(
                        partner.getUser().getId(),
                        RatingTargetType.DELIVERY_PARTNER,
                        RatingStatus.ACTIVE,
                        pageable
                )
                .map(
                        this::mapToResponse
                );
    }


    // =========================================================
    // BUSINESS SUMMARY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse getBusinessSummary(
            Long businessId
    ) {

        if (!businessRepository
                .existsById(
                        businessId
                )) {

            throw new BusinessException(
                    "Business not found",
                    404
            );
        }


        return buildBusinessSummary(
                businessId
        );
    }


    // =========================================================
    // DELIVERY PARTNER SUMMARY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse getDeliveryPartnerSummary(
            Long deliveryPartnerId
    ) {

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(
                                deliveryPartnerId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery partner not found",
                                        404
                                )
                        );


        return buildDeliveryPartnerSummary(
                deliveryPartnerId,
                partner.getUser().getId()
        );
    }


    // =========================================================
    // BUSINESS SUMMARY BUILDER
    // =========================================================

    private RatingSummaryResponse buildBusinessSummary(
            Long businessId
    ) {

        Double average =
                ratingRepository
                        .findBusinessAverageRating(
                                businessId,
                                RatingStatus.ACTIVE
                        );


        long total =
                ratingRepository
                        .countByTargetBusinessIdAndStatus(
                                businessId,
                                RatingStatus.ACTIVE
                        );


        return RatingSummaryResponse
                .builder()
                .targetId(
                        businessId
                )
                .targetType(
                        RatingTargetType.BUSINESS.name()
                )
                .averageRating(
                        average == null
                                ? 0.0
                                : roundAverage(average)
                )
                .totalRatings(
                        total
                )
                .oneStar(
                        countBusinessStars(
                                businessId,
                                (short) 1
                        )
                )
                .twoStar(
                        countBusinessStars(
                                businessId,
                                (short) 2
                        )
                )
                .threeStar(
                        countBusinessStars(
                                businessId,
                                (short) 3
                        )
                )
                .fourStar(
                        countBusinessStars(
                                businessId,
                                (short) 4
                        )
                )
                .fiveStar(
                        countBusinessStars(
                                businessId,
                                (short) 5
                        )
                )
                .build();
    }


    // =========================================================
    // PARTNER SUMMARY BUILDER
    // =========================================================

    private RatingSummaryResponse buildDeliveryPartnerSummary(
            Long deliveryPartnerId,
            Long userId
    ) {

        Double average =
                ratingRepository
                        .findDeliveryPartnerAverageRating(
                                userId,
                                RatingStatus.ACTIVE
                        );


        long total =
                ratingRepository
                        .countByTargetUserIdAndTargetTypeAndStatus(
                                userId,
                                RatingTargetType.DELIVERY_PARTNER,
                                RatingStatus.ACTIVE
                        );


        return RatingSummaryResponse
                .builder()
                .targetId(
                        deliveryPartnerId
                )
                .targetType(
                        RatingTargetType.DELIVERY_PARTNER.name()
                )
                .averageRating(
                        average == null
                                ? 0.0
                                : roundAverage(average)
                )
                .totalRatings(
                        total
                )
                .oneStar(
                        countPartnerStars(
                                userId,
                                (short) 1
                        )
                )
                .twoStar(
                        countPartnerStars(
                                userId,
                                (short) 2
                        )
                )
                .threeStar(
                        countPartnerStars(
                                userId,
                                (short) 3
                        )
                )
                .fourStar(
                        countPartnerStars(
                                userId,
                                (short) 4
                        )
                )
                .fiveStar(
                        countPartnerStars(
                                userId,
                                (short) 5
                        )
                )
                .build();
    }


    // =========================================================
    // PARTNER RATING REFRESH
    // =========================================================

private void refreshDeliveryPartnerRating(
        Long partnerId
) {

    DeliveryPartner partner =
            deliveryPartnerRepository
                    .findByIdForUpdate(
                            partnerId
                    )
                    .orElseThrow(() ->
                            new BusinessException(
                                    "Delivery partner not found",
                                    404
                            )
                    );

    Double average =
            ratingRepository
                    .findDeliveryPartnerAverageRating(
                            partner.getUser().getId(),
                            RatingStatus.ACTIVE
                    );

    partner.setRating(
            average == null
                    ? java.math.BigDecimal.ZERO
                    : java.math.BigDecimal.valueOf(
                            roundAverage(average)
                    )
    );

    deliveryPartnerRepository.save(partner);
}
    private void refreshDeliveryPartnerRatingByUserId(
            Long userId
    ) {

        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findByUserId(
                                userId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery partner not found",
                                        404
                                )
                        );


        Double average =
                ratingRepository
                        .findDeliveryPartnerAverageRating(
                                userId,
                                RatingStatus.ACTIVE
                        );


        partner.setRating(
                average == null
                        ? java.math.BigDecimal.ZERO
                        : java.math.BigDecimal
                                .valueOf(
                                        roundAverage(
                                                average
                                        )
                                )
        );


        deliveryPartnerRepository.save(
                partner
        );
    }


    // =========================================================
    // STAR COUNTS
    // =========================================================

    private long countBusinessStars(
            Long businessId,
            Short rating
    ) {

        return ratingRepository
                .countByTargetBusinessIdAndRatingAndStatus(
                        businessId,
                        rating,
                        RatingStatus.ACTIVE
                );
    }


    private long countPartnerStars(
            Long userId,
            Short rating
    ) {

        return ratingRepository
                .countByTargetUserIdAndTargetTypeAndRatingAndStatus(
                        userId,
                        RatingTargetType.DELIVERY_PARTNER,
                        rating,
                        RatingStatus.ACTIVE
                );
    }


    // =========================================================
    // SAVE WITH DB RACE PROTECTION
    // =========================================================

    private RatingResponse saveRatingSafely(
            Rating rating
    ) {

        try {

            Rating saved =
                    ratingRepository.save(
                            rating
                    );


            return mapToResponse(
                    saved
            );

        } catch (
                DataIntegrityViolationException ex
        ) {

            throw new BusinessException(
                    "Rating already exists for this order and target",
                    409
            );
        }
    }


    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateRequest(
            CreateRatingRequest request
    ) {

        if (request == null) {

            throw new BusinessException(
                    "Rating request is required",
                    400
            );
        }


        if (request.getTargetType() == null) {

            throw new BusinessException(
                    "Rating target type is required",
                    400
            );
        }


        if (request.getOrderId() == null) {

            throw new BusinessException(
                    "Order ID is required",
                    400
            );
        }


        if (request.getRating() == null
                || request.getRating() < 1
                || request.getRating() > 5) {

            throw new BusinessException(
                    "Rating must be between 1 and 5",
                    400
            );
        }


        if (request.getReview() != null
                && request.getReview().length() > 2000) {

            throw new BusinessException(
                    "Review cannot exceed 2000 characters",
                    400
            );
        }


        if (request.getTargetType()
                == RatingTargetType.BUSINESS) {

            if (request.getBusinessId() == null) {

                throw new BusinessException(
                        "Business ID is required",
                        400
                );
            }

            if (request.getDeliveryPartnerId() != null
                    || request.getDeliveryAssignmentId() != null) {

                throw new BusinessException(
                        "Delivery partner fields are not allowed for business rating",
                        400
                );
            }
        }


        if (request.getTargetType()
                == RatingTargetType.DELIVERY_PARTNER) {

            if (request.getDeliveryPartnerId() == null) {

                throw new BusinessException(
                        "Delivery partner ID is required",
                        400
                );
            }

            if (request.getDeliveryAssignmentId() == null) {

                throw new BusinessException(
                        "Delivery assignment ID is required",
                        400
                );
            }

            if (request.getBusinessId() != null) {

                throw new BusinessException(
                        "Business ID is not allowed for delivery partner rating",
                        400
                );
            }
        }
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new BusinessException(
                    "User is not authenticated",
                    401
            );
        }


        return userRepository
                .findByPhone(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Authenticated user not found",
                                401
                        )
                );
    }


    // =========================================================
    // PAGE VALIDATION
    // =========================================================

    private void validatePage(
            int page,
            int size
    ) {

        if (page < 0) {

            throw new BusinessException(
                    "Page cannot be negative",
                    400
            );
        }


        if (size < 1 || size > 100) {

            throw new BusinessException(
                    "Page size must be between 1 and 100",
                    400
            );
        }
    }


    // =========================================================
    // REVIEW NORMALIZATION
    // =========================================================

    private String normalizeReview(
            String review
    ) {

        if (review == null) {
            return null;
        }


        String normalized =
                review.trim();


        return normalized.isBlank()
                ? null
                : normalized;
    }


    // =========================================================
    // AVERAGE ROUNDING
    // =========================================================

    private double roundAverage(
            Double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private RatingResponse mapToResponse(
            Rating rating
    ) {

        Long businessId = null;

        String businessName = null;

        Long deliveryPartnerId = null;

        Long deliveryAssignmentId = null;


        if (rating.getTargetBusiness() != null) {

            businessId =
                    rating.getTargetBusiness()
                            .getId();

            businessName =
                    rating.getTargetBusiness()
                            .getName();
        }


        if (rating.getDeliveryAssignment() != null) {

            deliveryAssignmentId =
                    rating.getDeliveryAssignment()
                            .getId();

            deliveryPartnerId =
                    rating.getDeliveryAssignment()
                            .getPartner()
                            .getId();
        }


        return RatingResponse
                .builder()
                .id(
                        rating.getId()
                )
                .reviewerId(
                        rating.getReviewer()
                                .getId()
                )
                .reviewerName(
                        rating.getReviewer()
                                .getFullName()
                )
                .orderId(
                        rating.getOrder()
                                .getId()
                )
                .targetType(
                        rating.getTargetType()
                )
                .businessId(
                        businessId
                )
                .businessName(
                        businessName
                )
                .deliveryPartnerId(
                        deliveryPartnerId
                )
                .deliveryAssignmentId(
                        deliveryAssignmentId
                )
                .rating(
                        rating.getRating()
                )
                .review(
                        rating.getReview()
                )
                .status(
                        rating.getStatus()
                )
                .createdAt(
                        rating.getCreatedAt()
                )
                .updatedAt(
                        rating.getUpdatedAt()
                )
                .build();
    }
}