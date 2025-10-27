package com.ali.loomabackend.service;


import com.ali.loomabackend.exception.custom.InvalidTokenException;
import com.ali.loomabackend.exception.custom.ResourceNotFoundException;
import com.ali.loomabackend.model.entity.user.RefreshToken;
import com.ali.loomabackend.repository.user.RefreshTokenRepository;
import com.ali.loomabackend.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    @Transactional
    public RefreshToken createRefreshToken(UserDetailsImpl userDetails, HttpServletRequest request) {

        long activeTokens = refreshTokenRepository.countValidTokensByUser(userDetails.getUserId(), LocalDateTime.now());

        if (activeTokens >= 3) {
            revokeOldestToken(userDetails);
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userDetails.getUserId())
                .token(token)
                .expiryDate(expiryDate)
                .userAgent(extractUserAgent(request))
                .ipAddress(extractIpAddress(request))
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", userDetails.getUsername());

        return savedToken;
    }


    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Refresh token is missing or empty.");
        }
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
    }


    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (refreshToken.getRevoked()) {
            log.warn("Attempted to use revoked refresh token for userId: {}", refreshToken.getUserId());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("Attempted to use expired refresh token for userId: {}. Deleting token.", refreshToken.getId());
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token has expired. Please login again");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        try {
            RefreshToken refreshToken = findByToken(token);
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Revoked refresh token for userId: {}", refreshToken.getId());
        } catch (ResourceNotFoundException e) {
            log.warn("Attempted to revoke a refresh token that does not exist: {}", token);
            // Do not throw an exception, just log it. Client is already logged out.
        }
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked all refresh tokens for userId: {}", userId);
    }

    @Transactional
    public void deleteAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
        log.info("Deleted all refresh tokens for userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> getValidUserTokens(UserDetailsImpl userDetails) {
        return refreshTokenRepository.findValidTokensByUser(userDetails.getUserId(), LocalDateTime.now());
    }

    @Transactional
    protected void revokeOldestToken(UserDetailsImpl userDetails) {
        List<RefreshToken> validTokens = getValidUserTokens(userDetails);
        if (!validTokens.isEmpty()) {
            RefreshToken oldestToken = validTokens.stream()
                    .min(Comparator.comparing(RefreshToken::getCreatedAt))
                    .orElse(null);

            oldestToken.setRevoked(true);
            refreshTokenRepository.save(oldestToken);
            log.info("Revoked oldest refresh token for user: {}", userDetails.getUsername());
        }
    }


    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("Completed cleanup of expired refresh tokens older than {}", now);
    }

    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.length() > 255 // Changed to 255 for standard DB varchar
                ? userAgent.substring(0, 255)
                : userAgent;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
