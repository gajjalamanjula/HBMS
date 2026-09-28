package com.hms.hms.modules.announcement.controller;

import com.hms.hms.modules.announcement.dto.*;
import com.hms.hms.modules.announcement.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    public String create(
           @Valid @RequestBody CreateAnnouncementRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        return announcementService.createAnnouncement(
                request,
                email
        );
    }

    @GetMapping
    public List<AnnouncementResponse> get(
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        return announcementService.getAnnouncements(email);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable UUID id,
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        return announcementService.deleteAnnouncement(id, email);
    }
}