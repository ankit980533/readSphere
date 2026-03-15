package com.novelplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * File Storage Service - Handles file uploads
 * Supports both local storage and AWS S3
 */
@Service
public class FileStorageService {
    
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;
    
    @Autowired(required = false)
    private S3StorageService s3StorageService;
    
    private final String localUploadDir = "uploads/";
    
    /**
     * Upload a file (PDF, image, etc.)
     * Uses S3 if enabled, otherwise stores locally
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (s3Enabled && s3StorageService != null && s3StorageService.isConfigured()) {
            log.info("Uploading file to AWS S3: {}", file.getOriginalFilename());
            return s3StorageService.uploadFile(file, folder);
        } else {
            log.info("Uploading file to local storage: {}", file.getOriginalFilename());
            return uploadFileLocally(file, folder);
        }
    }
    
    /**
     * Upload PDF novel
     */
    public String uploadNovelPdf(MultipartFile file) throws IOException {
        return uploadFile(file, "novels/pdfs");
    }
    
    /**
     * Upload cover image
     */
    public String uploadCoverImage(MultipartFile file) throws IOException {
        return uploadFile(file, "novels/covers");
    }
    
    /**
     * Upload user profile picture
     */
    public String uploadProfilePicture(MultipartFile file) throws IOException {
        return uploadFile(file, "users/profiles");
    }
    
    /**
     * Store file locally (fallback when S3 is not configured)
     */
    private String uploadFileLocally(MultipartFile file, String folder) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(localUploadDir + folder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        // Return relative path
        return folder + "/" + fileName;
    }
    
    /**
     * Check if S3 is enabled
     */
    public boolean isS3Enabled() {
        return s3Enabled && s3StorageService != null && s3StorageService.isConfigured();
    }
    
    /**
     * Get storage type
     */
    public String getStorageType() {
        return isS3Enabled() ? "AWS S3" : "Local Storage";
    }
}
