package com.hms.hms.modules.review.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BranchReviewSummaryDTO {

    private Double averageFoodRating;
    private Double averageCleaningRating;
    private Double averageSecurityRating;
    private Double averageWifiRating;
    private Double averagePowerRating;

    private Double overallRating;

    private Long totalReviews;



    private List<ReviewResponseDTO> reviews;
}
