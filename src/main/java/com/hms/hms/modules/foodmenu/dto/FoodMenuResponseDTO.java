package com.hms.hms.modules.foodmenu.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodMenuResponseDTO {

    private UUID id;
    private String fileUrl;
    private UUID uploadedBy;
    private LocalDate weekStartDate;
    private LocalDateTime createdAt;
}