package com.ali.loomabackend.model.entity.user;

import com.ali.loomabackend.model.enums.FollowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
        indexes = {
                @Index(name = "idx_follower_id", columnList = "follower_id"),
                @Index(name = "idx_following_id", columnList = "following_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_follower_status", columnList = "follower_id, status"),
                @Index(name = "idx_following_status", columnList = "following_id, status")
        }
)
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🎯 Simple FK columns - NO @ManyToOne
    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "following_id", nullable = false)
    private UUID followingId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FollowStatus status = FollowStatus.ACTIVE;

    @Column(name = "notify_on_post")
    @Builder.Default
    private Boolean notifyOnPost = true;

    // Validation to prevent self-following
    @PrePersist
    @PreUpdate
    private void validateNotSelfFollow() {
        if (followerId != null && followingId != null
                && followerId.equals(followingId)) {
            throw new IllegalStateException("User cannot follow themselves");
        }
    }
}
