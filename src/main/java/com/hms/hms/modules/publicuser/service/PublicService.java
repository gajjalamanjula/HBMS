package com.hms.hms.modules.publicuser.service;

import com.hms.hms.modules.branch.dto.BranchResponse;
import com.hms.hms.modules.branch.entity.Branch;
import com.hms.hms.modules.publicuser.dto.*;
import com.hms.hms.modules.room.entity.Bed;

import java.util.List;
import java.util.UUID;

public interface PublicService {

    String register(PublicRegisterRequest request);

    List<BranchResponse> getBranches();

    List<BedResponse> getBeds(UUID branchId);
    BookingResponse book(BookingRequest request);

    String payFullAmount(UUID bookingId);
    List<BranchResponse> getMenHostels(String location);

    List<BranchResponse> getWomenHostels(String location);
    List<BranchResponse> getCoLivingHostels(String location);
    List<RoomAvailabilityResponse> getAvailableRooms(
            UUID branchId
    );
    List<BedResponse> getBedsByRoom(UUID roomId);
}