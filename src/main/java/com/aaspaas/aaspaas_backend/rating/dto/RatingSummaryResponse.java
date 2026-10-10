package com.aaspaas.aaspaas_backend.rating.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingSummaryResponse {

    private Long targetId;

    private String targetType;

    private Double averageRating;

    private Long totalRatings;

    private Long oneStar;

    private Long twoStar;

    private Long threeStar;

    private Long fourStar;

    private Long fiveStar;
}