package com.hms.hms.modules.room.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String roomNumber;
    private Integer floor;
    private String roomType;
    private Integer totalBeds;
    private String name;
    private UUID branchId;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}