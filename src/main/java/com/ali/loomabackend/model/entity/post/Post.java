package com.ali.loomabackend.model.entity.post;

import com.ali.loomabackend.model.enums.post.PostVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_author_id", columnList = "author_id"),
        @Index(name = "idx_post_visibility", columnList = "visibility"),
        @Index(name = "idx_post_created_at", columnList = "created_at"),
        @Index(name = "idx_post_deleted", columnList = "deleted")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private Long shareCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void incrementShareCount() {
        this.shareCount++;
    }

    public boolean isVisible() {
        return visibility == PostVisibility.PUBLIC && !deleted;
    }
}