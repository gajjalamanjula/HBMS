package com.hms.hms.modules.foodmenu.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "food_menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID branchId;

    private String fileUrl;

    private LocalDate weekStartDate;

    private UUID uploadedBy;

    private Boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;
}