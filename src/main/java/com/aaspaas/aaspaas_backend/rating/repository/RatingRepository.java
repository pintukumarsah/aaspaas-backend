package com.aaspaas.aaspaas_backend.rating.repository;

import com.aaspaas.aaspaas_backend.rating.entity.Rating;
import com.aaspaas.aaspaas_backend.rating.enums.RatingStatus;
import com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository
        extends JpaRepository<Rating, Long> {


    // =========================================================
    // EXISTING RATING - BUSINESS
    // =========================================================

    Optional<Rating>
    findByReviewerIdAndOrderIdAndTargetBusinessId(
            Long reviewerId,
            Long orderId,
            Long businessId
    );


    // =========================================================
    // EXISTING RATING - DELIVERY PARTNER
    // =========================================================

    Optional<Rating>
    findByReviewerIdAndOrderIdAndTargetUserIdAndDeliveryAssignmentId(
            Long reviewerId,
            Long orderId,
            Long targetUserId,
            Long deliveryAssignmentId
    );


    // =========================================================
    // LOCK RATING FOR UPDATE
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Rating r
        JOIN FETCH r.order o
        WHERE r.id = :id
          AND r.reviewer.id = :reviewerId
        """)
    Optional<Rating> findByIdForUpdate(
            @Param("id")
            Long id,

            @Param("reviewerId")
            Long reviewerId
    );


    // =========================================================
    // BUSINESS REVIEWS
    // =========================================================

    Page<Rating>
    findByTargetBusinessIdAndStatusOrderByCreatedAtDesc(
            Long businessId,
            RatingStatus status,
            Pageable pageable
    );


    // =========================================================
    // DELIVERY PARTNER REVIEWS
    // =========================================================

    Page<Rating>
    findByTargetUserIdAndTargetTypeAndStatusOrderByCreatedAtDesc(
            Long targetUserId,
            RatingTargetType targetType,
            RatingStatus status,
            Pageable pageable
    );


    // =========================================================
    // BUSINESS AVERAGE
    // =========================================================

    @Query("""
        SELECT AVG(r.rating)
        FROM Rating r
        WHERE r.targetBusiness.id = :businessId
          AND r.status = :status
        """)
    Double findBusinessAverageRating(
            @Param("businessId")
            Long businessId,

            @Param("status")
            RatingStatus status
    );


    // =========================================================
    // BUSINESS COUNT
    // =========================================================

    long countByTargetBusinessIdAndStatus(
            Long businessId,
            RatingStatus status
    );


    // =========================================================
    // DELIVERY PARTNER AVERAGE
    // =========================================================

    @Query("""
        SELECT AVG(r.rating)
        FROM Rating r
        WHERE r.targetUser.id = :userId
          AND r.targetType =
              com.aaspaas.aaspaas_backend.rating.enums.RatingTargetType.DELIVERY_PARTNER
          AND r.status = :status
        """)
    Double findDeliveryPartnerAverageRating(
            @Param("userId")
            Long userId,

            @Param("status")
            RatingStatus status
    );


    // =========================================================
    // DELIVERY PARTNER COUNT
    // =========================================================

    long countByTargetUserIdAndTargetTypeAndStatus(
            Long userId,
            RatingTargetType targetType,
            RatingStatus status
    );


    // =========================================================
    // BUSINESS STAR COUNTS
    // =========================================================

    long countByTargetBusinessIdAndRatingAndStatus(
            Long businessId,
            Short rating,
            RatingStatus status
    );


    // =========================================================
    // DELIVERY PARTNER STAR COUNTS
    // =========================================================

    long countByTargetUserIdAndTargetTypeAndRatingAndStatus(
            Long userId,
            RatingTargetType targetType,
            Short rating,
            RatingStatus status
    );
}