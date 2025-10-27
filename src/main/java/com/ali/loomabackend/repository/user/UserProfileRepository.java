package com.ali.loomabackend.repository.user;

import com.ali.loomabackend.model.entity.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    List<UserProfile> findByLocation(String location);

    @Query("SELECT p FROM UserProfile p WHERE p.userId IN :userIds")
    List<UserProfile> findAllByUserIds(@Param("userIds") List<UUID> userIds);

    @Query("SELECT p FROM UserProfile p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<UserProfile> searchByName(@Param("search") String search);
}