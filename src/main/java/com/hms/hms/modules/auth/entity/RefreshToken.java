package com.hms.hms.modules.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //  FIX
    private UUID id;

    private String token;

    private String email;

    private boolean revoked;

    private LocalDateTime expiryDate;
}
