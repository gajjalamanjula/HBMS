package com.hms.hms.modules.announcement.service;

import com.hms.hms.modules.announcement.dto.*;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {

    String createAnnouncement(
            CreateAnnouncementRequest request,
            String email
    );

    List<AnnouncementResponse> getAnnouncements(String email);

    String deleteAnnouncement(UUID id, String email);
}