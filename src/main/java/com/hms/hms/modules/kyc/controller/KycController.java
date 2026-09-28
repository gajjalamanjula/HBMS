package com.hms.hms.modules.kyc.controller;

import com.hms.hms.modules.kyc.dto.KycResponse;
import com.hms.hms.modules.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    //  UPLOAD
    @PostMapping("/upload")
    public KycResponse upload(
            @RequestParam UUID userId,
            @RequestParam MultipartFile aadhaar_file,
            @RequestParam MultipartFile photo_file) {

        return kycService.uploadKyc(userId, aadhaar_file, photo_file);
    }

    // GET FULL KYC
    @GetMapping("/{userId}")
    public KycResponse get(@PathVariable UUID userId) {
        return kycService.getKyc(userId);
    }

    //  GET AADHAAR
    @GetMapping("/{userId}/aadhaar")
    public String getAadhaar(@PathVariable UUID userId) {
        return kycService.getAadhaar(userId);
    }

    //  GET PHOTO
    @GetMapping("/{userId}/photo")
    public String getPhoto(@PathVariable UUID userId) {
        return kycService.getPhoto(userId);
    }

    //  UPDATE
    @PutMapping("/{userId}")
    public KycResponse update(
            @PathVariable UUID userId,
            @RequestParam MultipartFile aadhaar_file,
            @RequestParam MultipartFile photo_file) {

        return kycService.updateKyc(userId, aadhaar_file, photo_file);
    }
}