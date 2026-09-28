package com.hms.hms.modules.branch.dto;

import com.hms.hms.common.enums.HostelType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BranchResponse {

    private UUID id;



    private String location;

    private String imageUrl;

    private Double monthlyRent;

    private HostelType hostelType;

    // PUBLIC HOSTEL DETAILS

    private String hostelName;

    private String city;

    private String area;

    private String colony;

    private String roadNo;

    private String landmark;

    private String fullAddress;

    private String description;

    private String managerName;

    private String managerPhone;

    private String whatsappNumber;

    private Integer totalFloors;

    private String amenities;

    private String mapLocationUrl;

    private List<String> imageUrls;
}