package com.ali.loomabackend.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class FileValidator {

    private final Tika tika = new Tika();
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public boolean isSupportedImageType(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String actualMimeType = tika.detect(inputStream);
            return ALLOWED_IMAGE_TYPES.contains(actualMimeType);
        } catch (IOException e) {
            // Log the error
            return false;
        }
    }
}