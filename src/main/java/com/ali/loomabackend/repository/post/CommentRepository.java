package com.ali.loomabackend.repository.post;

import com.ali.loomabackend.model.entity.post.Comment;
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

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Optional<Comment> findByIdAndDeletedFalse(UUID id);

    List<Comment> findAllByPostIdAndDeletedFalse(UUID postId);

    List<Comment> findAllByAuthorIdAndDeletedFalse(UUID authorId);

    List<Comment> findAllByParentCommentIdAndDeletedFalse(UUID parentCommentId);

    // ========== Replies (Comments with Parent) ==========


    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentId " +
            "AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentAllStatuses(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId IN :parentIds " +
            "AND c.deleted = false " +
            "ORDER BY c.parentCommentId, c.createdAt ASC")
    List<Comment> findRepliesByParentCommentIds(
            @Param("parentIds") List<UUID> parentIds);


    @Query("SELECT c FROM Comment c WHERE c.authorId = :authorId " +
            "AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.authorId = :authorId " +
            "AND c.postId = :postId AND c.deleted = false " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorIdAndPostId(
            @Param("authorId") UUID authorId,
            @Param("postId") UUID postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId " +
            "AND c.deleted = false")
    long countByPostIdAllStatuses(@Param("postId") UUID postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentCommentId = :parentId " +
            "AND c.deleted = false")
    long countRepliesByParentIdAllStatuses(@Param("parentId") UUID parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.authorId = :authorId " +
            "AND c.deleted = false")
    long countByAuthorId(@Param("authorId") UUID authorId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 " +
            "WHERE c.id = :commentId AND c.likeCount > 0")
    void decrementLikeCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :commentId")
    void incrementReplyCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount - 1 " +
            "WHERE c.id = :commentId AND c.replyCount > 0")
    void decrementReplyCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.deletedAt = :deletedAt " +
            "WHERE c.id = :commentId")
    void softDelete(@Param("commentId") UUID commentId, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.deletedAt = :deletedAt " +
            "WHERE c.parentCommentId = :parentId")
    void softDeleteAllReplies(@Param("parentId") UUID parentId, @Param("deletedAt") Instant deletedAt);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.deletedAt = :deletedAt " +
            "WHERE c.postId = :postId")
    void softDeleteAllByPostId(@Param("postId") UUID postId, @Param("deletedAt") Instant deletedAt);

    boolean existsByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndAuthorIdAndDeletedFalse(UUID id, UUID authorId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Comment c WHERE c.postId = :postId AND c.authorId = :authorId " +
            "AND c.deleted = false")
    boolean hasUserCommentedOnPost(@Param("postId") UUID postId, @Param("authorId") UUID authorId);
}