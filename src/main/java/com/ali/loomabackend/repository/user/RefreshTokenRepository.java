package com.ali.loomabackend.repository.user;


import com.ali.loomabackend.model.entity.user.RefreshToken;
import com.ali.loomabackend.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId " +
            "AND rt.tokenVersion = :version AND rt.revoked = false")
    List<RefreshToken> findValidTokensByUserIdAndVersion(
            @Param("userId") UUID userId,
            @Param("version") Integer version);


    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
            "WHERE rt.userId = :userId " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > :now")
    long countValidTokensByUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);


    @Query("SELECT rt FROM RefreshToken rt " +
            "WHERE rt.userId = :userId " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
