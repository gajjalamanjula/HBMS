package com.hms.hms.modules.branch.entity;

import com.hms.hms.common.enums.HostelType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private UUID ownerId;


    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    private Double monthlyRent;

    private Double bookingAdvance;

    @Enumerated(EnumType.STRING)
    private HostelType hostelType;

    private String hostelName;

    private String city;

    private String area;

    private String colony;

    private String roadNo;

    private String landmark;

    @Column(columnDefinition = "TEXT")
    private String fullAddress;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String managerName;

    private String managerPhone;

    private String whatsappNumber;

    private Integer totalFloors;

    @Column(columnDefinition = "TEXT")
    private String amenities;

    @Column(columnDefinition = "TEXT")
    private String mapLocationUrl;

    @ElementCollection
    @CollectionTable(
            name = "branch_image_urls",
            joinColumns = @JoinColumn(name = "branch_id")
    )
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}

