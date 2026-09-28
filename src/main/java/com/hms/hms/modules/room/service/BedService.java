package com.hms.hms.modules.room.service;

import com.hms.hms.modules.room.dto.AssignBedRequest;
import com.hms.hms.modules.room.entity.Bed;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BedService {

    List<Bed> getAvailableBedsByUser(String email);

    void assignBed(
            AssignBedRequest request,
            String email
    );

    void updateBedStatus(UUID bedId, String status);

    Map<String, Object> getBedStatsByUser(String email);
}