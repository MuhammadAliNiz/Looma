package com.ali.loomabackend.repository.user;

import com.ali.loomabackend.model.entity.user.UserStats;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {

    Optional<UserStats> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    @Query("SELECT s FROM UserStats s WHERE s.userId IN :userIds")
    List<UserStats> findAllByUserIds(@Param("userIds") List<UUID> userIds);

    @Query("SELECT s FROM UserStats s WHERE s.followersCount >= :minFollowers " +
            "ORDER BY s.followersCount DESC")
    List<UserStats> findTopUsersByFollowers(@Param("minFollowers") Long minFollowers);

    @Transactional
    @Modifying
    @Query("UPDATE UserStats us SET us.postsCount = us.postsCount + 1 WHERE us.userId = :userId")
    void incrementPostsCount(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE UserStats us SET us.postsCount = us.postsCount - 1 WHERE us.userId = :userId AND us.postsCount > 0")
    void decrementPostsCount(@Param("userId") UUID userId);
}