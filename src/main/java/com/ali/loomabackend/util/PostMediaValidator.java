package com.ali.loomabackend.util;


import com.ali.loomabackend.model.enums.post.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PostMediaValidator {

    private final Tika tika = new Tika();

    @Value("${app.media.max-image-size:10485760}") // 10MB default
    private long maxImageSize;

    @Value("${app.media.max-video-size:104857600}") // 100MB default
    private long maxVideoSize;

    @Value("${app.media.max-document-size:20971520}") // 20MB default
    private long maxDocumentSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/svg+xml"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-ms-wmv",
            "video/webm"
    );


    public MediaType validateAndDetectMediaType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String actualMimeType;

        try (InputStream inputStream = file.getInputStream()) {
            actualMimeType = tika.detect(inputStream, file.getOriginalFilename());
            log.debug("Detected MIME type: {} for file: {}", actualMimeType, file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error detecting MIME type for file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Failed to read file for validation");
        }

        if (ALLOWED_IMAGE_TYPES.contains(actualMimeType)) {
            validateFileSize(file, maxImageSize, "Image");
            return MediaType.IMAGE;
        } else if (ALLOWED_VIDEO_TYPES.contains(actualMimeType)) {
            validateFileSize(file, maxVideoSize, "Video");
            return MediaType.VIDEO;
        } else {
            log.warn("Unsupported MIME type: {} for file: {}", actualMimeType, file.getOriginalFilename());
            throw new IllegalArgumentException(
                    "Unsupported file type: " + actualMimeType + ". " +
                            "Allowed types: Images (JPEG, PNG, GIF, WebP), Videos (MP4, MOV, AVI), Documents (PDF, Word, Excel)"
            );
        }
    }


    public Map<MultipartFile, MediaType> validateMultipleFiles(List<MultipartFile> files) {
        if (files.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 files allowed per post");
        }

        Map<MultipartFile, MediaType> validatedFiles = new HashMap<>();
        for (MultipartFile file : files) {
            MediaType mediaType = validateAndDetectMediaType(file);
            validatedFiles.put(file, mediaType);
        }

        return validatedFiles;
    }

    public boolean isSupportedImageType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String actualMimeType = tika.detect(inputStream, file.getOriginalFilename());
            return ALLOWED_IMAGE_TYPES.contains(actualMimeType);
        } catch (IOException e) {
            log.error("Error validating image type", e);
            return false;
        }
    }

    public boolean isSupportedVideoType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String actualMimeType = tika.detect(inputStream, file.getOriginalFilename());
            return ALLOWED_VIDEO_TYPES.contains(actualMimeType);
        } catch (IOException e) {
            log.error("Error validating video type", e);
            return false;
        }
    }

    public String getMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream, file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error detecting MIME type", e);
            return null;
        }
    }


    private void validateFileSize(MultipartFile file, long maxSize, String fileType) {
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("%s file size exceeds maximum limit of %d bytes. File size: %d bytes",
                            fileType, maxSize, file.getSize())
            );
        }
    }
}