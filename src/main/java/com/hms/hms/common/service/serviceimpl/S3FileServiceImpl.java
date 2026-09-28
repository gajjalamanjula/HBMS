package com.hms.hms.common.service.serviceimpl;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@Profile("prod")
public class S3FileServiceImpl implements FileService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    private String getFolder(FileType type) {
        return switch (type) {
            case KYC_IMAGE -> "images/kyc/";
            case MENU -> "documents/menus/";
            case INVOICE -> "documents/invoices/";
            case BRANCH_PHOTO -> "images/branches/";
            case ROOM_PHOTO -> "images/rooms/";
        };
    }

    private String uploadToS3(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                throw new CustomException("File is empty");
            }

            String contentType = file.getContentType();

            if (contentType == null ||
                    (!contentType.equals("application/pdf") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpeg") &&
                            !contentType.equals("image/jpg"))) {
                throw new CustomException("Only PDF, PNG, JPG, JPEG files are allowed");
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = folder + fileName;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return cloudFrontDomain + "/" + key;

        } catch (Exception e) {
            throw new CustomException("S3 upload failed: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, FileType type) {
        return uploadToS3(file, getFolder(type));
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);

            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(key)
            );
        } catch (Exception e) {
            throw new CustomException("S3 delete failed");
        }
    }
}