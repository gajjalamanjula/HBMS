package com.hms.hms.modules.room.controller;

import com.hms.hms.modules.room.dto.*;
import com.hms.hms.modules.room.entity.Bed;
import com.hms.hms.modules.room.service.BedService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping("/available")
    public List<Bed> available(HttpServletRequest request) {

        String email = (String) request.getAttribute("userEmail");

        return bedService.getAvailableBedsByUser(email);
    }
    @PostMapping("/assign")
    public String assign(
           @Valid @RequestBody AssignBedRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                (String) httpRequest.getAttribute("userEmail");

        bedService.assignBed(request, email);

        return "Bed assigned";
    }

    @PatchMapping("/{id}/status")
    public String update( @PathVariable UUID id,
                          @Valid @RequestBody BedStatusUpdateRequest request) {
        bedService.updateBedStatus(id, request.getStatus());
        return "Status updated";
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(HttpServletRequest request) {

        String email = (String) request.getAttribute("userEmail");

        return bedService.getBedStatsByUser(email);
    }
}