package com.hms.hms.modules.publicuser.controller;

import com.hms.hms.modules.booking.service.BookingService;
import com.hms.hms.modules.branch.dto.BranchResponse;

import com.hms.hms.modules.publicuser.dto.*;
import com.hms.hms.modules.publicuser.service.PublicService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;
    private final BookingService bookingService;
    // REGISTER PUBLIC USER
    @PostMapping("/register")
    public String register(
            @Valid @RequestBody PublicRegisterRequest request
    ) {
        return publicService.register(request);
    }

    @PostMapping("/pay-full/{bookingId}")
    public String payFullAmount(
            @PathVariable UUID bookingId
    ) {
        return publicService.payFullAmount(bookingId);
    }
    // GET ALL BRANCHES
    @GetMapping("/branches")
    public List<BranchResponse> getBranches(){
        return publicService.getBranches();
    }

    @GetMapping("/rooms/{roomId}/beds")
    public List<BedResponse> getBedsByRoom(
            @PathVariable UUID roomId
    ) {

        return publicService.getBedsByRoom(roomId);
    }

    // GET AVAILABLE BEDS
    @GetMapping("/branches/{branchId}/beds")
    public List<BedResponse> getBeds(
            @PathVariable UUID branchId
    ) {
        return publicService.getBeds(branchId);
    }

    @GetMapping("/branches/{branchId}/rooms")
    public List<RoomAvailabilityResponse> getAvailableRooms(
            @PathVariable UUID branchId
    ) {

        return publicService.getAvailableRooms(branchId);
    }

    // BOOKING REQUEST
    @PostMapping("/book")
    public BookingResponse book(
           @Valid @RequestBody BookingRequest request
    ) {
        return publicService.book(request);
    }

}