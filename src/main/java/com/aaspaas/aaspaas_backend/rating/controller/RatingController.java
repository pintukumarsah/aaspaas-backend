package com.aaspaas.aaspaas_backend.rating.controller;

import com.aaspaas.aaspaas_backend.common.dto.ApiResponse;
import com.aaspaas.aaspaas_backend.rating.dto.CreateRatingRequest;
import com.aaspaas.aaspaas_backend.rating.dto.RatingResponse;
import com.aaspaas.aaspaas_backend.rating.dto.RatingSummaryResponse;
import com.aaspaas.aaspaas_backend.rating.service.RatingService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>>
    createRating(
            @Valid
            @RequestBody
            CreateRatingRequest request
    ) {

        RatingResponse response =
                ratingService.createRating(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Rating created successfully",
                                response
                        )
                );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>>
    updateRating(
            @PathVariable
            Long ratingId,

            @Valid
            @RequestBody
            CreateRatingRequest request
    ) {

        RatingResponse response =
                ratingService.updateRating(
                        ratingId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rating updated successfully",
                        response
                )
        );
    }


    // =========================================================
    // SOFT DELETE
    // =========================================================

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteRating(
            @PathVariable
            Long ratingId
    ) {

        ratingService.deleteRating(
                ratingId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rating deleted successfully",
                        null
                )
        );
    }


    // =========================================================
    // GET SINGLE
    // =========================================================

    @GetMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>>
    getRating(
            @PathVariable
            Long ratingId
    ) {

        RatingResponse response =
                ratingService.getRating(
                        ratingId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rating fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // BUSINESS REVIEWS
    // =========================================================

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>>
    getBusinessRatings(
            @PathVariable
            Long businessId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "20"
            )
            int size
    ) {

        Page<RatingResponse> response =
                ratingService.getBusinessRatings(
                        businessId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business ratings fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // BUSINESS SUMMARY
    // =========================================================

    @GetMapping("/business/{businessId}/summary")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>>
    getBusinessSummary(
            @PathVariable
            Long businessId
    ) {

        RatingSummaryResponse response =
                ratingService.getBusinessSummary(
                        businessId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business rating summary fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // DELIVERY PARTNER REVIEWS
    // =========================================================

    @GetMapping(
            "/delivery-partner/{deliveryPartnerId}"
    )
    public ResponseEntity<ApiResponse<Page<RatingResponse>>>
    getDeliveryPartnerRatings(
            @PathVariable
            Long deliveryPartnerId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "20"
            )
            int size
    ) {

        Page<RatingResponse> response =
                ratingService.getDeliveryPartnerRatings(
                        deliveryPartnerId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery partner ratings fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // DELIVERY PARTNER SUMMARY
    // =========================================================

    @GetMapping(
            "/delivery-partner/{deliveryPartnerId}/summary"
    )
    public ResponseEntity<ApiResponse<RatingSummaryResponse>>
    getDeliveryPartnerSummary(
            @PathVariable
            Long deliveryPartnerId
    ) {

        RatingSummaryResponse response =
                ratingService.getDeliveryPartnerSummary(
                        deliveryPartnerId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery partner rating summary fetched successfully",
                        response
                )
        );
    }
}