package com.ali.loomabackend.repository.post;

import com.ali.loomabackend.model.entity.post.Like;
import com.ali.loomabackend.model.enums.post.LikeTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByUserIdAndTargetTypeAndTargetId(
            UUID userId,
            LikeTargetType targetType,
            UUID targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(
            UUID userId,
            LikeTargetType targetType,
            UUID targetId);

    @Query("SELECT l FROM Like l WHERE l.targetType = :targetType " +
            "AND l.targetId = :targetId ORDER BY l.createdAt DESC")
    Page<Like> findByTargetTypeAndTargetId(
            @Param("targetType") LikeTargetType targetType,
            @Param("targetId") UUID targetId,
            Pageable pageable);

    @Query("SELECT l.userId FROM Like l WHERE l.targetType = :targetType " +
            "AND l.targetId = :targetId")
    List<UUID> findUserIdsByTarget(
            @Param("targetType") LikeTargetType targetType,
            @Param("targetId") UUID targetId);

    @Query("SELECT l FROM Like l WHERE l.userId = :userId " +
            "AND l.targetType = :targetType ORDER BY l.createdAt DESC")
    Page<Like> findByUserIdAndTargetType(
            @Param("userId") UUID userId,
            @Param("targetType") LikeTargetType targetType,
            Pageable pageable);

    long countByTargetTypeAndTargetId(LikeTargetType targetType, UUID targetId);

    void deleteByUserIdAndTargetTypeAndTargetId(
            UUID userId,
            LikeTargetType targetType,
            UUID targetId);

    @Query("SELECT l.targetId FROM Like l WHERE l.userId = :userId " +
            "AND l.targetType = :targetType")
    List<UUID> findTargetIdsByUserIdAndTargetType(
            @Param("userId") UUID userId,
            @Param("targetType") LikeTargetType targetType);

    void deleteAllByTargetTypeAndTargetId(LikeTargetType targetType, UUID targetId);
}