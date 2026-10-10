package com.aaspaas.aaspaas_backend.rating.service;

import com.aaspaas.aaspaas_backend.rating.dto.CreateRatingRequest;
import com.aaspaas.aaspaas_backend.rating.dto.RatingResponse;
import com.aaspaas.aaspaas_backend.rating.dto.RatingSummaryResponse;

import org.springframework.data.domain.Page;

public interface RatingService {


    RatingResponse createRating(
            CreateRatingRequest request
    );


    RatingResponse updateRating(
            Long ratingId,
            CreateRatingRequest request
    );


    void deleteRating(
            Long ratingId
    );


    RatingResponse getRating(
            Long ratingId
    );


    Page<RatingResponse> getBusinessRatings(
            Long businessId,
            int page,
            int size
    );


    Page<RatingResponse> getDeliveryPartnerRatings(
            Long deliveryPartnerId,
            int page,
            int size
    );


    RatingSummaryResponse getBusinessSummary(
            Long businessId
    );


    RatingSummaryResponse getDeliveryPartnerSummary(
            Long deliveryPartnerId
    );
}