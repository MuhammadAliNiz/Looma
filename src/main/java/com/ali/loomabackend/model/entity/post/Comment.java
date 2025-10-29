package com.ali.loomabackend.model.entity.post;

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
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
        @Index(name = "idx_comment_created_at", columnList = "created_at"),
        @Index(name = "idx_comment_deleted", columnList = "deleted")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "reply_count", nullable = false)
    @Builder.Default
    private Long replyCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Boolean edited = false;

    @Column(name = "edited_at")
    private Instant editedAt;

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

    // Helper methods
    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementReplyCount() {
        this.replyCount++;
    }

    public void decrementReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public void markAsEdited() {
        this.edited = true;
        this.editedAt = Instant.now();
    }
}