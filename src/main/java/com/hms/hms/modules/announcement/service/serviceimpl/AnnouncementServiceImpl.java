package com.hms.hms.modules.announcement.service.serviceimpl;

import com.hms.hms.common.enums.Role;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.modules.announcement.dto.*;
import com.hms.hms.modules.announcement.entity.Announcement;
import com.hms.hms.modules.announcement.repository.AnnouncementRepository;
import com.hms.hms.modules.announcement.service.AnnouncementService;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl
        implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public String createAnnouncement(
            CreateAnnouncementRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        if (!(user.getRole() == Role.MANAGER ||
                user.getRole() == Role.OWNER)) {

            throw new CustomException(
                    "Only OWNER or MANAGER allowed",
                    HttpStatus.FORBIDDEN
            );
        }

        if (user.getBranch() == null) {
            throw new CustomException(
                    "User not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .branchId(user.getBranch().getId())
                .createdBy(user.getId())
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        announcementRepository.save(announcement);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Announcement Created",
                        "New announcement created successfully"
                )
        );
        return "Announcement created successfully";
    }

    @Override
    public List<AnnouncementResponse> getAnnouncements(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.getBranch() == null) {
            throw new CustomException(
                    "User not assigned to branch",
                    HttpStatus.BAD_REQUEST
            );
        }

        return announcementRepository
                .findByBranchIdAndActiveTrueOrderByCreatedAtDesc(
                        user.getBranch().getId()
                )
                .stream()
                .map(a -> AnnouncementResponse.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .message(a.getMessage())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public String deleteAnnouncement(UUID id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        Announcement announcement = announcementRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(
                        "Announcement not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.getBranch() == null ||
                !announcement.getBranchId()
                        .equals(user.getBranch().getId())) {

            throw new CustomException(
                    "Unauthorized",
                    HttpStatus.FORBIDDEN
            );
        }

        announcement.setActive(false);

        announcementRepository.save(announcement);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "Announcement Deleted",
                        "Announcement deleted successfully"
                )
        );
        return "Announcement deleted successfully";
    }
}