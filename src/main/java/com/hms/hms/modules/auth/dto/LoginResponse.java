package com.hms.hms.modules.auth.dto;

import lombok.*;

import java.util.UUID;


@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private UUID userId;
}
