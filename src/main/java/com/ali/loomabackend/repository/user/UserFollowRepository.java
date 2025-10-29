package com.ali.loomabackend.repository.user;


import com.ali.loomabackend.model.entity.user.UserFollow;
import com.ali.loomabackend.model.enums.user.FollowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query("SELECT uf.followingId FROM UserFollow uf WHERE uf.followerId = :userId " +
            "AND uf.status = :status")
    List<UUID> findFollowingIdsByFollowerId(
            @Param("userId") UUID userId,
            @Param("status") FollowStatus status);

    @Query("SELECT uf.followerId FROM UserFollow uf WHERE uf.followingId = :userId " +
            "AND uf.status = :status")
    List<UUID> findFollowerIdsByFollowingId(
            @Param("userId") UUID userId,
            @Param("status") FollowStatus status);

    Page<UserFollow> findByFollowerId(UUID followerId, Pageable pageable);

    Page<UserFollow> findByFollowingId(UUID followingId, Pageable pageable);

    long countByFollowerIdAndStatus(UUID followerId, FollowStatus status);

    long countByFollowingIdAndStatus(UUID followingId, FollowStatus status);
}
