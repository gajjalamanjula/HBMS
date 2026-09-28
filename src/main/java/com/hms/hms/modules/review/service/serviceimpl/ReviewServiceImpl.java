package com.hms.hms.modules.review.service.serviceimpl;

import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.util.SecurityUtil;
import com.hms.hms.modules.review.dto.BranchReviewSummaryDTO;
import com.hms.hms.modules.review.dto.ReviewRequestDTO;
import com.hms.hms.modules.review.dto.ReviewResponseDTO;
import com.hms.hms.modules.review.entity.Review;
import com.hms.hms.modules.review.repository.ReviewRepository;
import com.hms.hms.modules.review.service.ReviewService;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    public void addReview(ReviewRequestDTO request) {

        UUID tenantId = SecurityUtil.getCurrentUserId();
        UUID branchId = SecurityUtil.getCurrentBranchId();

        if (branchId == null) {
            throw new CustomException(
                    "Tenant not assigned to any branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        // One tenant -> one review
        if (reviewRepository
                .findByBranchIdAndTenantId(branchId, tenantId)
                .isPresent()) {

            throw new CustomException(
                    "You have already reviewed this hostel",
                    HttpStatus.BAD_REQUEST
            );
        }

        validateRatings(request);

        double overallRating = calculateOverallRating(request);

        Review review = Review.builder()
                .branchId(branchId)
                .tenantId(tenantId)
                .foodRating(request.getFoodRating())
                .cleaningRating(request.getCleaningRating())
                .securityRating(request.getSecurityRating())
                .wifiRating(request.getWifiRating())
                .powerRating(request.getPowerRating())
                .overallRating(overallRating)
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
    }

    @Override
    public void updateReview(
            UUID reviewId,
            ReviewRequestDTO request
    ) {

        UUID tenantId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new CustomException(
                                "Review not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (!review.getTenantId().equals(tenantId)) {
            throw new CustomException(
                    "Unauthorized to update review",
                    HttpStatus.FORBIDDEN
            );
        }

        validateRatings(request);

        review.setFoodRating(request.getFoodRating());
        review.setCleaningRating(request.getCleaningRating());
        review.setSecurityRating(request.getSecurityRating());
        review.setWifiRating(request.getWifiRating());
        review.setPowerRating(request.getPowerRating());
        review.setComment(request.getComment());

        review.setOverallRating(
                calculateOverallRating(request)
        );

        reviewRepository.save(review);
    }

    @Override
    public void deleteReview(UUID reviewId) {

        UUID tenantId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new CustomException(
                                "Review not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (!review.getTenantId().equals(tenantId)) {
            throw new CustomException(
                    "Unauthorized to delete review",
                    HttpStatus.FORBIDDEN
            );
        }

        reviewRepository.delete(review);
    }
    @Override
    public BranchReviewSummaryDTO getBranchReviewSummary(UUID branchId) {

        List<ReviewResponseDTO> reviews =
                reviewRepository
                        .findByBranchIdOrderByCreatedAtDesc(branchId)
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return BranchReviewSummaryDTO.builder()
                .averageFoodRating(
                        reviewRepository.getAverageFoodRating(branchId))
                .averageCleaningRating(
                        reviewRepository.getAverageCleaningRating(branchId))
                .averageSecurityRating(
                        reviewRepository.getAverageSecurityRating(branchId))
                .averageWifiRating(
                        reviewRepository.getAverageWifiRating(branchId))
                .averagePowerRating(
                        reviewRepository.getAveragePowerRating(branchId))
                .overallRating(
                        reviewRepository.getAverageOverallRating(branchId))
                .totalReviews(
                        reviewRepository.countByBranchId(branchId))
                .reviews(reviews)
                .build();
    }
    @Override
    public List<ReviewResponseDTO> getReviews(UUID branchId) {

        return reviewRepository
                .findByBranchIdOrderByCreatedAtDesc(branchId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private ReviewResponseDTO mapToDTO(Review review) {

        String tenantName = "Unknown";

        User tenant = userRepository
                .findById(review.getTenantId())
                .orElse(null);

        if (tenant != null) {
            tenantName = tenant.getName();
        }

        return ReviewResponseDTO.builder()
                .id(review.getId())
                .tenantName(tenantName)
                .foodRating(review.getFoodRating())
                .cleaningRating(review.getCleaningRating())
                .securityRating(review.getSecurityRating())
                .wifiRating(review.getWifiRating())
                .powerRating(review.getPowerRating())
                .overallRating(review.getOverallRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private double calculateOverallRating(
            ReviewRequestDTO request
    ) {

        return (
                request.getFoodRating()
                        + request.getCleaningRating()
                        + request.getSecurityRating()
                        + request.getWifiRating()
                        + request.getPowerRating()
        ) / 5.0;
    }

    private void validateRatings(
            ReviewRequestDTO request
    ) {

        validate(request.getFoodRating(), "Food rating");
        validate(request.getCleaningRating(), "Cleaning rating");
        validate(request.getSecurityRating(), "Security rating");
        validate(request.getWifiRating(), "WiFi rating");
        validate(request.getPowerRating(), "Power rating");
    }

    private void validate(
            Integer rating,
            String field
    ) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException(
                    field + " must be between 1 and 5",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}