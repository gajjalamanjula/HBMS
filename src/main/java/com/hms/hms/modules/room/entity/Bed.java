package com.hms.hms.modules.room.entity;

import com.hms.hms.common.enums.BedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "beds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID roomId;

    @Enumerated(EnumType.STRING)
    private BedStatus status;
    private String bedNumber;
    private UUID tenantId;
}
