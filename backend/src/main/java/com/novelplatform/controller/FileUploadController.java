package com.novelplatform.controller;

import com.novelplatform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

/**
 * File Upload Controller
 * Handles cover images and other file uploads
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Upload cover image for a novel
     */
    @PostMapping("/upload-cover")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    public ResponseEntity<Map<String, String>> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.uploadCoverImage(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("storageType", fileStorageService.getStorageType());
            response.put("message", "Cover image uploaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Upload profile picture
     */
    @PostMapping("/upload-profile")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.uploadProfilePicture(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("storageType", fileStorageService.getStorageType());
            response.put("message", "Profile picture uploaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get storage info
     */
    @GetMapping("/storage-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("storageType", fileStorageService.getStorageType());
        info.put("s3Enabled", fileStorageService.isS3Enabled());
        
        return ResponseEntity.ok(info);
    }
}
