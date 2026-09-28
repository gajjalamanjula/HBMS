package com.hms.hms.modules.review.controller;

import com.hms.hms.modules.review.dto.*;
import com.hms.hms.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public String addReview(
          @Valid @RequestBody ReviewRequestDTO request
    ) {

        reviewService.addReview(request);

        return "Review added successfully";
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    public String updateReview(
            @PathVariable UUID id,
          @Valid  @RequestBody ReviewRequestDTO request
    ) {

        reviewService.updateReview(id, request);

        return "Review updated successfully";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    public String deleteReview(
            @PathVariable UUID id
    ) {

        reviewService.deleteReview(id);

        return "Review deleted successfully";
    }

    @GetMapping("/branch/{branchId}")
    public List<ReviewResponseDTO> getReviews(
            @PathVariable UUID branchId
    ) {

        return reviewService.getReviews(branchId);
    }

    @GetMapping("/branch/{branchId}/summary")
    public BranchReviewSummaryDTO getBranchSummary(
            @PathVariable UUID branchId
    ) {

        return reviewService
                .getBranchReviewSummary(branchId);
    }
}
