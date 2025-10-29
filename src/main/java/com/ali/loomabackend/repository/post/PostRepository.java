package com.ali.loomabackend.repository.post;

import com.ali.loomabackend.model.entity.post.Post;
import com.ali.loomabackend.model.enums.post.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findByIdAndDeletedFalse(UUID id);

    Page<Post> findByAuthorIdAndDeletedFalse(UUID authorId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE  p.visibility = :visibility " +
            "AND p.deleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(
            @Param("visibility") PostVisibility visibility,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds " +
            "AND p.visibility = :visibility " +
            "AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPostsByAuthorIds(
            @Param("authorIds") List<UUID> authorIds,
            @Param("visibility") PostVisibility visibility,
            Pageable pageable);


    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " + // <-- Corrected line
            "AND p.visibility = :visibility " +
            "AND p.deleted = false")
    Page<Post> searchPosts(
            @Param("keyword") String keyword,
            @Param("visibility") PostVisibility visibility,
            Pageable pageable);



    @Query("SELECT COUNT(p) FROM Post p WHERE p.authorId = :authorId " +
            "AND p.deleted = false")
    long countByAuthorIdAndStatus(
            @Param("authorId") UUID authorId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 " +
            "WHERE p.id = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 " +
            "WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.deleted = true, p.deletedAt = :deletedAt " +
            "WHERE p.id = :postId")
    void softDelete(@Param("postId") UUID postId, @Param("deletedAt") Instant deletedAt);

    List<Post> findAllByAuthorIdAndDeletedFalse(UUID authorId);
}