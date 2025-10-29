package com.ali.loomabackend.model.dto.response.post;

import com.ali.loomabackend.model.enums.post.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    private UUID postId;

    private AuthorInfo author;

    private String content;

    private PostVisibility visibility;

    private Long likeCount;

    private Long commentCount;

    private Long shareCount;

    private List<MediaInfo> media;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant publishedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorInfo {
        private UUID userId;
        private String username;
        private String profilePictureUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MediaInfo {
        private UUID id;
        private UUID postId;
        private String mediaType;
        private String url;
    }
}
