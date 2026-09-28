package com.hms.hms.modules.foodmenu.service;

import com.hms.hms.modules.foodmenu.dto.FoodMenuResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FoodMenuService {

    void uploadMenu(MultipartFile file, LocalDate weekStartDate);

    FoodMenuResponseDTO getCurrentMenuForLoggedInUser();

    List<FoodMenuResponseDTO> getMenuHistoryForLoggedInUser();

    void deleteMenu(UUID id);

    FoodMenuResponseDTO getCurrentMenuByBranch(UUID branchId);

    List<FoodMenuResponseDTO> getMenuHistoryByBranch(UUID branchId);
}