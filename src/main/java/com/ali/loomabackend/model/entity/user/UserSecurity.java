package com.ali.loomabackend.model.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_security", indexes = {
        @Index(name = "idx_security_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_password_reset_token", columnList = "password_reset_token"),
        @Index(name = "idx_email_verification_token", columnList = "email_verification_token")
})
public class UserSecurity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🎯 Simple FK column - NO @OneToOne annotation
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private Instant passwordResetTokenExpiry;

    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private Instant emailVerificationTokenExpiry;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;

    @Column(name = "refresh_token_version", nullable = false)
    @Builder.Default
    private Integer refreshTokenVersion = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Helper methods
    public void lockAccount(Duration lockDuration) {
        this.accountLockedUntil = Instant.now().plus(lockDuration);
    }

    public boolean isAccountLocked() {
        return accountLockedUntil != null && Instant.now().isBefore(accountLockedUntil);
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void invalidateAllRefreshTokens() {
        this.refreshTokenVersion++;
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null
                && passwordResetTokenExpiry != null
                && Instant.now().isBefore(passwordResetTokenExpiry);
    }

    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null
                && emailVerificationTokenExpiry != null
                && Instant.now().isBefore(emailVerificationTokenExpiry);
    }
}