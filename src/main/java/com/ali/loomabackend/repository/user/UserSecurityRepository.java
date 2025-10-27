package com.ali.loomabackend.repository.user;

import com.ali.loomabackend.model.entity.user.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSecurityRepository extends JpaRepository<UserSecurity, UUID> {

    Optional<UserSecurity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    Optional<UserSecurity> findByPasswordResetToken(String token);

    Optional<UserSecurity> findByEmailVerificationToken(String token);

    @Query("SELECT s FROM UserSecurity s WHERE s.userId = :userId " +
            "AND s.refreshTokenVersion = :version")
    Optional<UserSecurity> findByUserIdAndTokenVersion(
            @Param("userId") UUID userId,
            @Param("version") Integer version);
}