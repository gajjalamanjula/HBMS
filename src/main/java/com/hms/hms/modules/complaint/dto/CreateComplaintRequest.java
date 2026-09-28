package com.hms.hms.modules.complaint.dto;

import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class CreateComplaintRequest {

    @NotBlank(message = "Category is required")
    @Size(min = 3, max = 50)
    private String category;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Priority must be LOW, MEDIUM or HIGH")
    private String priority;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500)
    private String description;
}