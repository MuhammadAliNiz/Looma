package com.ali.loomabackend.model.entity.post;

import com.ali.loomabackend.model.enums.post.MediaType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_media", indexes = {
        @Index(name = "idx_post_media_post_id", columnList = "post_id"),
        @Index(name = "idx_post_media_type", columnList = "media_type"),
        @Index(name = "idx_post_media_order", columnList = "display_order")
})
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Helper methods
    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }
}