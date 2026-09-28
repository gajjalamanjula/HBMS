package com.hms.hms.modules.branch.dto;

import com.hms.hms.common.enums.HostelType;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateBranchRequest {



    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 100, message = "Location must be 2-100 characters")
    private String location;

    private MultipartFile image;


    @NotNull(message = "Monthly rent is required")
    @Positive(message = "Monthly rent must be greater than zero")
    private Double monthlyRent;

    @NotNull(message = "Hostel type is required")
    private HostelType hostelType;

    @NotBlank(message = "Hostel name is required")
    private String hostelName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Manager phone must be 10 digits")
    private String managerPhone;

    @Pattern(regexp = "^[0-9]{10}$", message = "WhatsApp number must be 10 digits")
    private String whatsappNumber;

    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;

    private String city;

    private String area;

    private String colony;

    private String roadNo;

    private String landmark;

    private String fullAddress;

    private String description;

    private String managerName;


    private String amenities;

    private String mapLocationUrl;

    private List<MultipartFile> images;
}