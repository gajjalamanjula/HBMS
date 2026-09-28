package com.hms.hms.modules.room.dto;

import jakarta.persistence.Column;
import lombok.Data;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateRoomRequest {

    @NotBlank(message = "Room number required")
    private String roomNumber;

    @NotNull(message = "Floor required")
    private Integer floor;

    @NotBlank(message = "Room type required")
    private String roomType;

    @Min(value = 1, message = "At least 1 bed required")
    @Max(value = 10, message = "Max 10 beds allowed")
    private Integer totalBeds;

    private MultipartFile image;
}