package com.hms.hms.modules.branch.controller;

import com.hms.hms.modules.branch.dto.*;
import com.hms.hms.modules.branch.service.BranchService;
import com.hms.hms.modules.publicuser.service.PublicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final PublicService publicService;
    @PostMapping(consumes = "multipart/form-data")
    public BranchResponse createBranch(
           @Valid @ModelAttribute CreateBranchRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = (String) httpRequest.getAttribute("userEmail");
        return branchService.createBranch(request, email);
    }

    @GetMapping
    public List<BranchResponse> getBranches() {

        return branchService.getBranches();
    }

    @GetMapping("/men-hostels")
    public List<BranchResponse> getMenHostels(
            @RequestParam(required = false) String location
    ) {

        return publicService.getMenHostels(location);
    }

    @GetMapping("/women-hostels")
    public List<BranchResponse> getWomenHostels(
            @RequestParam(required = false) String location
    ) {

        return publicService.getWomenHostels(location);
    }
    @GetMapping("/co-living-hostels")
    public List<BranchResponse> getCoLivingHostels(
            @RequestParam(required = false) String location
    ) {

        return publicService.getCoLivingHostels(location);
    }

    @PutMapping(value = "/{branchId}", consumes = "multipart/form-data")
    public BranchResponse updateBranch(
            @PathVariable UUID branchId,
            @ModelAttribute CreateBranchRequest request
    ) {
        return branchService.updateBranch(branchId, request);
    }
}