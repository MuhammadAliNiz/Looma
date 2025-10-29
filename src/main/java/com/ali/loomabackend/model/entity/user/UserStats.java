package com.ali.loomabackend.model.entity.user;

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
@Table(name = "user_stats", indexes = {
        @Index(name = "idx_stats_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_followers_count", columnList = "followers_count"),
})
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private Long followersCount = 0L;

    @Column(name = "following_count", nullable = false)
    @Builder.Default
    private Long followingCount = 0L;

    @Column(name = "posts_count", nullable = false)
    @Builder.Default
    private Long postsCount = 0L;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Helper methods
    public void incrementFollowersCount() {
        this.followersCount++;
    }

    public void decrementFollowersCount() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void incrementFollowingCount() {
        this.followingCount++;
    }

    public void decrementFollowingCount() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }

    public void incrementPostsCount() {
        this.postsCount++;
    }

    public void decrementPostsCount() {
        if (this.postsCount > 0) {
            this.postsCount--;
        }
    }
}
