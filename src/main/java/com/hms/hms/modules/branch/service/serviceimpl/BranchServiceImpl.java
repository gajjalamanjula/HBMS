package com.hms.hms.modules.branch.service.serviceimpl;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.branch.dto.*;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.branch.repository.BranchRepository;
import com.hms.hms.modules.branch.service.BranchService;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.hms.hms.common.util.SecurityUtil;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public BranchResponse createBranch(CreateBranchRequest request, String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        if (!owner.getRole().equals(Role.OWNER)) {
            throw new CustomException("Only OWNER can create branch", HttpStatus.FORBIDDEN);
        }
        List<String> imagePaths = new ArrayList<>();

        if (request.getImages() != null && !request.getImages().isEmpty()) {

            imagePaths = request.getImages()
                    .stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> fileService.uploadFile(
                            file,
                            FileType.BRANCH_PHOTO
                    ))
                    .toList();

        } else if (request.getImage() != null && !request.getImage().isEmpty()) {

            String imagePath = fileService.uploadFile(
                    request.getImage(),
                    FileType.BRANCH_PHOTO
            );

            imagePaths = List.of(imagePath);
        }

        Branch branch = Branch.builder()


                .location(request.getLocation())

                .imageUrl(imagePaths.isEmpty() ? null : imagePaths.get(0))
                .imageUrls(imagePaths)

                .monthlyRent(request.getMonthlyRent())

                .hostelType(request.getHostelType())

                .hostelName(request.getHostelName())

                .city(request.getCity())

                .area(request.getArea())

                .colony(request.getColony())

                .roadNo(request.getRoadNo())

                .landmark(request.getLandmark())

                .fullAddress(request.getFullAddress())

                .description(request.getDescription())

                .managerName(request.getManagerName())

                .managerPhone(request.getManagerPhone())

                .whatsappNumber(request.getWhatsappNumber())

                .totalFloors(request.getTotalFloors())

                .amenities(request.getAmenities())

                .mapLocationUrl(request.getMapLocationUrl())

                .ownerId(owner.getId())

                .build();

        branchRepository.save(branch);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        owner.getId(),
                        "Branch Created",
                        "New branch created successfully"
                )
        );
        return BranchResponse.builder()


                .id(branch.getId())

                .hostelName(branch.getHostelName())

                .location(branch.getLocation())

                .imageUrl(branch.getImageUrl())

                .imageUrls(branch.getImageUrls())
                .monthlyRent(branch.getMonthlyRent())

                .hostelType(branch.getHostelType())

                .city(branch.getCity())

                .area(branch.getArea())

                .colony(branch.getColony())

                .roadNo(branch.getRoadNo())

                .landmark(branch.getLandmark())

                .fullAddress(branch.getFullAddress())

                .description(branch.getDescription())

                .managerName(branch.getManagerName())

                .managerPhone(branch.getManagerPhone())

                .whatsappNumber(branch.getWhatsappNumber())

                .totalFloors(branch.getTotalFloors())

                .amenities(branch.getAmenities())

                .mapLocationUrl(branch.getMapLocationUrl())

                .build();
    }

    @Override
    public List<BranchResponse> getBranches() {

        User user = userRepository.findById(
                SecurityUtil.getCurrentUserId()
        ).orElseThrow(() ->
                new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                )
        );

        // OWNER -> ALL BRANCHES
        if (user.getRole() == Role.OWNER) {

            return branchRepository.findByOwnerId(user.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        // MANAGER -> ONLY ASSIGNED BRANCH
        if (user.getRole() == Role.MANAGER) {

            Branch branch = branchRepository.findById(
                            user.getBranch().getId()
                    )
                    .orElseThrow(() ->
                            new CustomException(
                                    "Branch not found",
                                    HttpStatus.NOT_FOUND
                            )
                    );

            return List.of(mapToResponse(branch));
        }

        throw new CustomException(
                "Unauthorized",
                HttpStatus.FORBIDDEN
        );
    }

    private BranchResponse mapToResponse(Branch b) {

        return BranchResponse.builder()

                .id(b.getId())

                .hostelName(b.getHostelName())

                .location(b.getLocation())

                .imageUrl(b.getImageUrl())

                .imageUrls(b.getImageUrls())

                .monthlyRent(b.getMonthlyRent())

                .hostelType(b.getHostelType())

                .city(b.getCity())

                .area(b.getArea())

                .colony(b.getColony())

                .roadNo(b.getRoadNo())

                .landmark(b.getLandmark())

                .fullAddress(b.getFullAddress())

                .description(b.getDescription())

                .managerName(b.getManagerName())

                .managerPhone(b.getManagerPhone())

                .whatsappNumber(b.getWhatsappNumber())

                .totalFloors(b.getTotalFloors())

                .amenities(b.getAmenities())

                .mapLocationUrl(b.getMapLocationUrl())

                .build();
    }

    @Override
    public BranchResponse updateBranch(UUID branchId, CreateBranchRequest request) {

        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException(
                        "Branch not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(
                    "Only OWNER can update branch",
                    HttpStatus.FORBIDDEN
            );
        }

        if (!branch.getOwnerId().equals(user.getId())) {
            throw new CustomException(
                    "You can update only your own branch",
                    HttpStatus.FORBIDDEN
            );
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {

            List<String> imagePaths = request.getImages()
                    .stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> fileService.uploadFile(
                            file,
                            FileType.BRANCH_PHOTO
                    ))
                    .toList();

            branch.setImageUrls(imagePaths);

            branch.setImageUrl(
                    imagePaths.isEmpty()
                            ? null
                            : imagePaths.get(0)
            );

        } else if (request.getImage() != null && !request.getImage().isEmpty()) {

            String imagePath = fileService.uploadFile(
                    request.getImage(),
                    FileType.BRANCH_PHOTO
            );

            branch.setImageUrl(imagePath);

            branch.setImageUrls(List.of(imagePath));
        }

        branch.setLocation(request.getLocation());
        branch.setMonthlyRent(request.getMonthlyRent());
        branch.setHostelType(request.getHostelType());
        branch.setHostelName(request.getHostelName());
        branch.setCity(request.getCity());
        branch.setArea(request.getArea());
        branch.setColony(request.getColony());
        branch.setRoadNo(request.getRoadNo());
        branch.setLandmark(request.getLandmark());
        branch.setFullAddress(request.getFullAddress());
        branch.setDescription(request.getDescription());
        branch.setManagerName(request.getManagerName());
        branch.setManagerPhone(request.getManagerPhone());
        branch.setWhatsappNumber(request.getWhatsappNumber());
        branch.setTotalFloors(request.getTotalFloors());
        branch.setAmenities(request.getAmenities());
        branch.setMapLocationUrl(request.getMapLocationUrl());

        Branch savedBranch = branchRepository.save(branch);

        return mapToResponse(savedBranch);
    }
}
