package com.ali.loomabackend.model.entity.post;

import com.ali.loomabackend.model.enums.post.LikeTargetType;
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
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}),
        indexes = {
                @Index(name = "idx_like_user_id", columnList = "user_id"),
                @Index(name = "idx_like_target", columnList = "target_type, target_id"),
                @Index(name = "idx_like_created_at", columnList = "created_at")
        }
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private LikeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Helper methods
    public boolean isPostLike() {
        return targetType == LikeTargetType.POST;
    }

    public boolean isCommentLike() {
        return targetType == LikeTargetType.COMMENT;
    }
}