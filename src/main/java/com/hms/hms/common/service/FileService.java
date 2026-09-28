package com.hms.hms.common.service;

import com.hms.hms.common.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadFile(MultipartFile file, FileType type);

    void deleteFile(String fileUrl);
}