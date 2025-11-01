package com.ali.loomabackend.service;

import com.ali.loomabackend.exception.custom.ResourceNotFoundException;
import com.ali.loomabackend.model.dto.response.post.PostResponse;
import com.ali.loomabackend.model.entity.post.Post;
import com.ali.loomabackend.model.entity.post.PostMedia;
import com.ali.loomabackend.model.entity.user.UserProfile;
import com.ali.loomabackend.model.enums.post.MediaType;
import com.ali.loomabackend.model.enums.post.PostVisibility;
import com.ali.loomabackend.repository.post.PostMediaRepository;
import com.ali.loomabackend.repository.post.PostRepository;
import com.ali.loomabackend.repository.user.UserProfileRepository;
import com.ali.loomabackend.repository.user.UserStatsRepository;
import com.ali.loomabackend.security.UserDetailsImpl;
import com.ali.loomabackend.util.PostMediaValidator;
import com.ali.loomabackend.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final SecurityUtils securityUtils;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final S3Service s3Service;
    private final UserProfileRepository userProfileRepository;
    private final UserStatsRepository userStatsRepository;
    private final PostMediaValidator postMediaValidator;

    @Transactional
    public PostResponse createPost(String trimmedContent, PostVisibility visibility, Map<MultipartFile, MediaType> validatedFiles) {

        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new IllegalStateException("No authenticated user found")
        );

        Post post = Post.builder()
                .authorId(userDetails.getUserId())
                .content(trimmedContent)
                .visibility(visibility != null ? visibility : PostVisibility.PUBLIC)
                .publishedAt(Instant.now())
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Created post {} by user {}", savedPost.getId(), userDetails.getUserId());

        List<PostResponse.MediaInfo> mediaInfoList = new ArrayList<>();
        List<String> uploadedS3Keys = new ArrayList<>(); // Track for cleanup on failure

        if (validatedFiles != null && !validatedFiles.isEmpty()) {
            try {
                mediaInfoList = uploadPostMedia(
                        savedPost.getId(),
                        validatedFiles,
                        uploadedS3Keys,
                        userDetails.getUserId()
                );
            } catch (Exception e) {
                log.error("Failed to upload media for post {}, rolling back", savedPost.getId(), e);
                cleanupUploadedFiles(uploadedS3Keys);
                throw new IllegalStateException("Failed to upload media files: " + e.getMessage(), e);
            }
        }

        userStatsRepository.incrementPostsCount(userDetails.getUserId());

        UserProfile userProfile = userProfileRepository.findByUserId(savedPost.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found for user ID: " + savedPost.getAuthorId()));

        return PostResponse.builder()
                .postId(savedPost.getId())
                .author(PostResponse.AuthorInfo.builder()
                        .userId(userDetails.getUserId())
                        .username(userDetails.getActualUsername())
                        .profilePictureUrl(s3Service.getFileUrl(userProfile.getProfilePictureUrl()))
                        .build())
                .content(savedPost.getContent())
                .visibility(savedPost.getVisibility())
                .likeCount(savedPost.getLikeCount())
                .commentCount(savedPost.getCommentCount())
                .shareCount(savedPost.getShareCount())
                .media(mediaInfoList)
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(savedPost.getUpdatedAt())
                .publishedAt(savedPost.getPublishedAt())
                .build();
    }


    private List<PostResponse.MediaInfo> uploadPostMedia(
            UUID postId,
            Map<MultipartFile, MediaType> validatedFiles,
            List<String> uploadedS3Keys,
            UUID userId) throws IOException {

        List<PostResponse.MediaInfo> mediaInfoList = new ArrayList<>();
        AtomicInteger displayOrder = new AtomicInteger(0);

        for (Map.Entry<MultipartFile, MediaType> entry : validatedFiles.entrySet()) {
            MultipartFile file = entry.getKey();
            MediaType mediaType = entry.getValue();

            String actualMimeType = postMediaValidator.getMimeType(file);

            String s3Key = s3Service.uploadFile(file, "post", userId.toString());
            uploadedS3Keys.add(s3Key); // Track for potential cleanup

            PostMedia postMedia = PostMedia.builder()
                    .postId(postId)
                    .mediaType(mediaType)
                    .s3Key(s3Key)
                    .fileSize(file.getSize())
                    .mimeType(actualMimeType)
                    .displayOrder(displayOrder.getAndIncrement())
                    .build();

            PostMedia savedPostMedia = postMediaRepository.save(postMedia);

            PostResponse.MediaInfo mediaInfo = PostResponse.MediaInfo.builder()
                    .id(savedPostMedia.getId())
                    .postId(savedPostMedia.getPostId())
                    .mediaType(savedPostMedia.getMediaType().name())
                    .url(s3Service.getFileUrl(savedPostMedia.getS3Key()))
                    .build();

            mediaInfoList.add(mediaInfo);

            log.info("Uploaded media {} for post {}: {} ({} bytes)",
                    savedPostMedia.getId(),
                    postId,
                    mediaType,
                    file.getSize());
        }

        return mediaInfoList;
    }

    private void cleanupUploadedFiles(List<String> s3Keys) {
        for (String s3Key : s3Keys) {
            try {
                s3Service.deleteFile(s3Key);
                log.info("Cleaned up orphaned S3 file: {}", s3Key);
            } catch (Exception e) {
                log.error("Failed to cleanup S3 file: {}", s3Key, e);
            }
        }
    }

    @Transactional
    public void deletePost(UUID postId) {
        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new IllegalStateException("No authenticated user found")
        );


        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthorId().equals(userDetails.getUserId())) {
            throw new IllegalStateException("You don't have permission to delete this post");
        }

        List<String> s3Keys = postMediaRepository.findS3KeysByPostId(postId);

        postRepository.softDelete(postId, Instant.now());
        postMediaRepository.softDeleteAllByPostId(postId);

        for (String s3Key : s3Keys) {
            try {
                s3Service.deleteFile(s3Key);
            } catch (Exception e) {
                log.error("Failed to delete S3 file: {}", s3Key, e);
            }
        }

        userStatsRepository.decrementPostsCount(userDetails.getUserId());

        log.info("Deleted post {} and {} media files", postId, s3Keys.size());
    }

}
