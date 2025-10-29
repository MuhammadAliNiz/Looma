package com.ali.loomabackend.repository.post;

import com.ali.loomabackend.model.entity.post.PostMedia;
import com.ali.loomabackend.model.enums.post.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {

    List<PostMedia> findByPostIdAndDeletedFalseOrderByDisplayOrderAsc(UUID postId);

    List<PostMedia> findByPostIdAndMediaTypeAndDeletedFalse(UUID postId, MediaType mediaType);

    Optional<PostMedia> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT pm FROM PostMedia pm WHERE pm.postId = :postId " +
            "AND pm.deleted = false ORDER BY pm.displayOrder ASC")
    List<PostMedia> findActiveMediaByPost(@Param("postId") UUID postId);

    long countByPostIdAndDeletedFalse(UUID postId);

    long countByPostIdAndMediaTypeAndDeletedFalse(UUID postId, MediaType mediaType);

    @Modifying
    @Query("UPDATE PostMedia pm SET pm.deleted = true WHERE pm.id = :mediaId")
    void softDelete(@Param("mediaId") UUID mediaId);

    @Modifying
    @Query("UPDATE PostMedia pm SET pm.deleted = true WHERE pm.postId = :postId")
    void softDeleteAllByPostId(@Param("postId") UUID postId);

    void deleteAllByPostId(UUID postId);

    @Query("SELECT pm.s3Key FROM PostMedia pm WHERE pm.postId = :postId " +
            "AND pm.deleted = false")
    List<String> findS3KeysByPostId(@Param("postId") UUID postId);

    @Query("SELECT pm FROM PostMedia pm WHERE pm.s3Key = :s3Key " +
            "AND pm.deleted = false")
    Optional<PostMedia> findByS3Key(@Param("s3Key") String s3Key);
}