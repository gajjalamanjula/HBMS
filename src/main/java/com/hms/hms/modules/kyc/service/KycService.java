package com.hms.hms.modules.kyc.service;

import com.hms.hms.modules.kyc.dto.KycResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface KycService {

    KycResponse uploadKyc(UUID userId, MultipartFile aadhaar, MultipartFile photo);

    KycResponse getKyc(UUID userId);

    String getAadhaar(UUID userId);

    String getPhoto(UUID userId);

    KycResponse updateKyc(UUID userId, MultipartFile aadhaar, MultipartFile photo);


}