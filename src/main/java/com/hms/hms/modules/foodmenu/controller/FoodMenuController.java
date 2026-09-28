package com.hms.hms.modules.foodmenu.controller;

import com.hms.hms.modules.foodmenu.dto.FoodMenuResponseDTO;
import com.hms.hms.modules.foodmenu.service.FoodMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/food-menu")
@RequiredArgsConstructor
public class FoodMenuController {

    private final FoodMenuService foodMenuService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> uploadMenu(
          @Valid @RequestParam("file") MultipartFile file,
            @RequestParam("week_start_date") LocalDate weekStartDate
    ) {

        foodMenuService.uploadMenu(file, weekStartDate);

        return ResponseEntity.ok("Menu uploaded successfully");
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('MANAGER','TENANT','STAFF')")
    public ResponseEntity<FoodMenuResponseDTO> getCurrentMenu() {

        return ResponseEntity.ok(
                foodMenuService.getCurrentMenuForLoggedInUser()
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('MANAGER','TENANT','STAFF')")
    public ResponseEntity<List<FoodMenuResponseDTO>> getHistory() {

        return ResponseEntity.ok(
                foodMenuService.getMenuHistoryForLoggedInUser()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> deleteMenu(@PathVariable UUID id) {

        foodMenuService.deleteMenu(id);

        return ResponseEntity.ok("Menu deleted successfully");
    }

    @GetMapping("/owner/current/{branchId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FoodMenuResponseDTO> getCurrentMenuByBranch(
            @PathVariable UUID branchId
    ) {

        return ResponseEntity.ok(
                foodMenuService.getCurrentMenuByBranch(branchId)
        );
    }

    @GetMapping("/owner/history/{branchId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<FoodMenuResponseDTO>> getHistoryByBranch(
            @PathVariable UUID branchId
    ) {

        return ResponseEntity.ok(
                foodMenuService.getMenuHistoryByBranch(branchId)
        );
    }
}
