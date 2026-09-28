package com.hms.hms.modules.kyc.service.serviceimpl;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import com.hms.hms.modules.kyc.dto.KycResponse;
import com.hms.hms.modules.kyc.service.KycService;
import com.hms.hms.modules.notification.event.NotificationEvent;
import com.hms.hms.modules.user.entity.User;
import com.hms.hms.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final UserRepository userRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;
    //  UPLOAD KYC
    @Override
    public KycResponse uploadKyc(UUID userId,
                                 MultipartFile aadhaar,
                                 MultipartFile photo) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        //  VALIDATION (recommended)
        if (aadhaar == null || aadhaar.isEmpty()) {
            throw new CustomException("Aadhaar file is required", HttpStatus.BAD_REQUEST);
        }

        if (photo == null || photo.isEmpty()) {
            throw new CustomException("Photo is required", HttpStatus.BAD_REQUEST);
        }

        // UPLOAD USING NEW METHOD
        String aadhaarPath = fileService.uploadFile(aadhaar, FileType.KYC_IMAGE);
        String photoPath   = fileService.uploadFile(photo, FileType.KYC_IMAGE);

        //  SAVE
        user.setAadhaarUrl(aadhaarPath);
        user.setPhotoUrl(photoPath);
        user.setKycVerified(false);

        userRepository.save(user);
        eventPublisher.publishEvent(
                new NotificationEvent(
                        user.getId(),
                        "KYC Uploaded",
                        "Your KYC documents uploaded successfully"
                )
        );
        return KycResponse.builder()
                .aadhaarUrl(aadhaarPath)
                .photoUrl(photoPath)
                .verified(false)
                .build();
    }

    //  GET FULL KYC
    @Override
    public KycResponse getKyc(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        return KycResponse.builder()
                .aadhaarUrl(user.getAadhaarUrl())
                .photoUrl(user.getPhotoUrl())
                .verified(user.getKycVerified())
                .build();
    }

    //  GET AADHAAR
    @Override
    public String getAadhaar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND))
                .getAadhaarUrl();
    }

    //  GET PHOTO
    @Override
    public String getPhoto(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND))
                .getPhotoUrl();
    }

    //  UPDATE KYC
    @Override
    public KycResponse updateKyc(UUID userId,
                                 MultipartFile aadhaar,
                                 MultipartFile photo) {

        return uploadKyc(userId, aadhaar, photo);
    }
}
