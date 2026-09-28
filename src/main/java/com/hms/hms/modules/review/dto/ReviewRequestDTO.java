package com.hms.hms.modules.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequestDTO {
    @Min(value = 1, message = "Food rating must be at least 1")
    @Max(value = 5, message = "Food rating cannot exceed 5")
    private Integer foodRating;

    @Min(value = 1, message = "Cleaning rating must be at least 1")
    @Max(value = 5, message = "Cleaning rating cannot exceed 5")
    private Integer cleaningRating;

    @Min(value = 1, message = "Security rating must be at least 1")
    @Max(value = 5, message = "Security rating cannot exceed 5")
    private Integer securityRating;

    @Min(value = 1, message = "Wifi rating must be at least 1")
    @Max(value = 5, message = "Wifi rating cannot exceed 5")
    private Integer wifiRating;

    @Min(value = 1, message = "Power rating must be at least 1")
    @Max(value = 5, message = "Power rating cannot exceed 5")
    private Integer powerRating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
}