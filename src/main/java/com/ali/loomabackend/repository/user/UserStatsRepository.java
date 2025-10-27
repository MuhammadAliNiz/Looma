package com.ali.loomabackend.repository.user;

import com.ali.loomabackend.model.entity.user.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
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
}