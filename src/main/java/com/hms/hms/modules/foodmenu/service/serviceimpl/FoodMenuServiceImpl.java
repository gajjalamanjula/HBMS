package com.hms.hms.modules.foodmenu.service.serviceimpl;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.config.CustomUserDetails;
import com.hms.hms.modules.foodmenu.dto.FoodMenuResponseDTO;
import com.hms.hms.modules.foodmenu.entity.FoodMenu;
import com.hms.hms.modules.foodmenu.repository.FoodMenuRepository;
import com.hms.hms.modules.foodmenu.service.FoodMenuService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodMenuServiceImpl implements FoodMenuService {

    private final FoodMenuRepository foodMenuRepository;
    private final FileService fileService;

    @Override
    public void uploadMenu(MultipartFile file, LocalDate weekStartDate) {

        UUID branchId = getCurrentUserBranchId();
        UUID userId = getCurrentUserId();

        // STEP 1: FILE VALIDATION
        if (file == null || file.isEmpty()) {
            throw new CustomException(
                    "File is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 5MB validation
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new CustomException(
                    "File size must be less than 5MB",
                    HttpStatus.BAD_REQUEST
            );
        }

        // File type validation
        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("application/pdf")
                        || contentType.startsWith("image/"))) {

            throw new CustomException(
                    "Only PDF or Image allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        // STEP 2: DATE VALIDATION
        if (weekStartDate == null) {
            throw new CustomException(
                    "Week start date is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (weekStartDate.isAfter(LocalDate.now())) {
            throw new CustomException(
                    "Week date cannot be in future",
                    HttpStatus.BAD_REQUEST
            );
        }

        // STEP 3: DUPLICATE CHECK
        if (foodMenuRepository.existsByBranchIdAndWeekStartDate(
                branchId,
                weekStartDate
        )) {

            throw new CustomException(
                    "Menu already uploaded for this week",
                    HttpStatus.BAD_REQUEST
            );
        }

        // STEP 4: FILE UPLOAD
        String fileUrl = fileService.uploadFile(file, FileType.MENU);

        // STEP 5: DEACTIVATE OLD MENUS
        foodMenuRepository.deactivateOldMenus(branchId);

        // STEP 6: SAVE NEW MENU
        FoodMenu menu = FoodMenu.builder()
                .branchId(branchId)
                .fileUrl(fileUrl)
                .weekStartDate(weekStartDate)
                .uploadedBy(userId)
                .isActive(true)
                .build();

        foodMenuRepository.save(menu);
    }

    @Override
    public FoodMenuResponseDTO getCurrentMenuForLoggedInUser() {

        UUID branchId = getCurrentUserBranchId();

        FoodMenu menu = foodMenuRepository
                .findTopByBranchIdOrderByWeekStartDateDesc(branchId)
                .orElseThrow(() ->
                        new CustomException(
                                "Menu not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        return mapToDTO(menu);
    }

    @Override
    public List<FoodMenuResponseDTO> getMenuHistoryForLoggedInUser() {

        UUID branchId = getCurrentUserBranchId();

        return foodMenuRepository
                .findTop8ByBranchIdOrderByWeekStartDateDesc(branchId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    @Override
    public FoodMenuResponseDTO getCurrentMenuByBranch(UUID branchId) {

        FoodMenu menu = foodMenuRepository
                .findTopByBranchIdOrderByWeekStartDateDesc(branchId)
                .orElseThrow(() ->
                        new CustomException(
                                "Menu not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        return mapToDTO(menu);
    }

    @Override
    public List<FoodMenuResponseDTO> getMenuHistoryByBranch(UUID branchId) {

        return foodMenuRepository
                .findTop8ByBranchIdOrderByWeekStartDateDesc(branchId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void deleteMenu(UUID id) {

        UUID branchId = getCurrentUserBranchId();

        FoodMenu menu = foodMenuRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Menu not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        // SECURITY CHECK
        if (!menu.getBranchId().equals(branchId)) {

            throw new CustomException(
                    "Unauthorized to delete this menu",
                    HttpStatus.FORBIDDEN
            );
        }



        // DELETE FILE FROM STORAGE
        fileService.deleteFile(menu.getFileUrl());

        // DELETE DB RECORD
        foodMenuRepository.delete(menu);
    }

    private FoodMenuResponseDTO mapToDTO(FoodMenu menu) {

        FoodMenuResponseDTO dto = new FoodMenuResponseDTO();

        dto.setId(menu.getId());
        dto.setFileUrl(menu.getFileUrl());
        dto.setUploadedBy(menu.getUploadedBy());
        dto.setWeekStartDate(menu.getWeekStartDate());
        dto.setCreatedAt(menu.getCreatedAt());

        return dto;
    }

    // TEMPORARY METHODS
    // Replace later using JWT + SecurityContext

    private UUID getCurrentUserBranchId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        if (user.getBranchId() == null) {
            throw new CustomException("User is not assigned to any branch", HttpStatus.BAD_REQUEST);
        }

        return user.getBranchId();
    }

    private UUID getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        return user.getId();
    }
}