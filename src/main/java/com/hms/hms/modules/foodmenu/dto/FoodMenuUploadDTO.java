package com.hms.hms.modules.foodmenu.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class FoodMenuUploadDTO {

    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;

}