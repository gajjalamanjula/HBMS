package com.hms.hms.modules.review.service;

import com.hms.hms.modules.review.dto.*;

import java.util.*;

public interface ReviewService {

    void addReview(ReviewRequestDTO request);

    void updateReview(
            UUID reviewId,
            ReviewRequestDTO request
    );

    void deleteReview(UUID reviewId);

    List<ReviewResponseDTO> getReviews(
            UUID branchId
    );
    BranchReviewSummaryDTO getBranchReviewSummary(UUID branchId);
}