package com.hms.hms.modules.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponseDTO {

    private UUID id;

    private String tenantName;

    private Integer foodRating;

    private Integer cleaningRating;

    private Integer securityRating;

    private Integer wifiRating;

    private Integer powerRating;

    private Double overallRating;

    private String comment;

    private LocalDateTime createdAt;
}