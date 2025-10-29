package com.ali.loomabackend.service;


import com.ali.loomabackend.exception.custom.BusinessException;
import com.ali.loomabackend.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadFile(MultipartFile file, String folder, String userId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String key = folder + userId + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .serverSideEncryption(ServerSideEncryption.AES256) // Enable encryption
                    .acl(ObjectCannedACL.PUBLIC_READ) // Assuming public read is desired
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded successfully to S3: {}", key);
            return key;

        } catch (S3Exception e) {
            log.error("Error uploading file to S3: {}", e.awsErrorDetails().errorMessage(), e);
            // Throw a generic RuntimeException
            throw new BusinessException(ErrorCode.MEDIA_UPLOAD_FAILED,
                    "Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());        }
        // Allow IOException from file.getBytes() to propagate
    }

    public String getFileUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return String.format("https://%s.%s.digitaloceanspaces.com/%s", bucketName, region, key);
    }

    public String generatePresignedUrl(String key, Duration duration) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            log.error("Error generating presigned URL: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL");
        }
    }

    public void deleteFile(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("Attempted to delete file with null or empty key.");
            return;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", key);

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new BusinessException(ErrorCode.MEDIA_UPLOAD_FAILED,
                    "Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());        }
    }

    public boolean fileExists(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking file existence: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to check file existence");
        }
    }

    public Map<String, String> getFileMetadata(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty to fetch metadata.");
        }
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.metadata();

        } catch (S3Exception e) {
            log.error("Error getting file metadata: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to get file metadata");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}

