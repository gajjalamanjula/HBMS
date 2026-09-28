package com.hms.hms.common.service.serviceimpl;

import com.hms.hms.common.enums.FileType;
import com.hms.hms.common.exception.CustomException;
import com.hms.hms.common.service.FileService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("!prod")
public class FileServiceImpl implements FileService {

    private final String BASE = System.getProperty("user.dir") + "/uploads/";

    private String getFolder(FileType type) {
        return switch (type) {
            case KYC_IMAGE -> "images/kyc/";
            case MENU -> "documents/menus/";
            case INVOICE -> "documents/invoices/";
            case BRANCH_PHOTO -> "images/branches/";
            case ROOM_PHOTO -> "images/rooms/";
        };
    }

    private String saveFile(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // VALIDATION ADDED
            String contentType = file.getContentType();

            if (contentType == null ||
                    (!contentType.equals("application/pdf") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpeg") &&
                            !contentType.equals("image/jpg"))) {

                throw new CustomException("Only PDF, PNG, JPG, JPEG files are allowed");
            }

            File dir = new File(BASE + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String fullPath = BASE + folder + fileName;

            file.transferTo(new File(fullPath));

            return folder + fileName;

        } catch (Exception e) {
            throw new CustomException("File upload failed: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, FileType type) {
        String folder = getFolder(type);
        return saveFile(file, folder);
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }

            Path basePath = Paths.get(BASE).toAbsolutePath().normalize();

            Path targetPath = basePath
                    .resolve(fileUrl)
                    .normalize();

            if (!targetPath.startsWith(basePath)) {
                throw new CustomException("Invalid file path");
            }

            File file = targetPath.toFile();

            if (file.exists() && file.isFile()) {
                file.delete();
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(
                    "File deletion failed: " + e.getMessage()
            );
        }
    }
}
