package com.ali.loomabackend.controller;

import com.ali.loomabackend.model.dto.response.ApiResponse;
import com.ali.loomabackend.model.dto.response.post.PostResponse;
import com.ali.loomabackend.model.enums.post.MediaType;
import com.ali.loomabackend.model.enums.post.PostVisibility;
import com.ali.loomabackend.service.PostService;
import com.ali.loomabackend.util.PostMediaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostMediaValidator postMediaValidator;
    private  final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestPart(value = "content", required = false) String content,
            @RequestPart(value = "visibility", required = false) PostVisibility visibility,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
            ) {

        if ((content == null || content.trim().isEmpty()) && (mediaFiles == null || mediaFiles.isEmpty())) {
                throw new IllegalArgumentException("Post must have either content or media");
        }

        String trimmedContent = content != null ? content.trim() : null;

        if (trimmedContent != null && trimmedContent.length() > 10000) {
            throw new IllegalArgumentException("Content exceeds maximum length of 10000 characters");
        }

        Map<MultipartFile, MediaType> validatedFiles = null;

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            validatedFiles = postMediaValidator.validateMultipleFiles(mediaFiles);
        }

        PostResponse postResponse = postService.createPost(trimmedContent, visibility, validatedFiles);

        ApiResponse<PostResponse> apiResponse = ApiResponse.success(postResponse, "Post retrieved successfully.");

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        ApiResponse<Void> apiResponse = ApiResponse.success(null, "Post deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

}
