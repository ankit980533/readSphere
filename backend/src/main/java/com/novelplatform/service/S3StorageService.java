package com.novelplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 Storage Service
 * Handles file uploads to Amazon S3
 */
@Service
public class S3StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);
    
    @Value("${aws.s3.bucket:}")
    private String bucketName;
    
    @Value("${aws.s3.region:ap-south-1}")
    private String region;
    
    @Value("${aws.credentials.access-key:}")
    private String accessKeyId;
    
    @Value("${aws.credentials.secret-key:}")
    private String secretAccessKey;
    
    @Value("${aws.s3.enabled:false}")
    private boolean enabled;
    
    private S3Client s3Client;
    
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("AWS S3 is disabled. Using local storage.");
            return;
        }
        
        if (accessKeyId == null || accessKeyId.isEmpty() || 
            secretAccessKey == null || secretAccessKey.isEmpty()) {
            log.warn("AWS credentials not configured. S3 storage will not be available.");
            return;
        }
        
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            
            log.info("✅ AWS S3 configured successfully");
            log.info("   Bucket: {}", bucketName);
            log.info("   Region: {}", region);
        } catch (Exception e) {
            log.error("❌ Failed to initialize AWS S3: {}", e.getMessage());
        }
    }
    
    /**
     * Upload file to S3 and return the public URL
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("AWS S3 is not configured");
        }
        
        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)  // Make file publicly accessible
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, fileName);
            
            log.info("File uploaded to S3: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage());
            throw new IOException("Failed to upload file to S3", e);
        }
    }
    
    /**
     * Check if S3 is properly configured
     */
    public boolean isConfigured() {
        return enabled && s3Client != null;
    }
}
