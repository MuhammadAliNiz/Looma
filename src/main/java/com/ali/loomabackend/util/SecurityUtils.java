package com.ali.loomabackend.util;


import com.ali.loomabackend.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public final class SecurityUtils {
    public Optional<UserDetailsImpl> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return Optional.of((UserDetailsImpl) principal);
        }

        return Optional.empty();
    }

    public Optional<UUID> getCurrentUserId() {
        return getCurrentUserDetails()
                .map(UserDetailsImpl::getUserId);
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentUserDetails()
                .map(UserDetailsImpl::getActualUsername);
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentUserDetails()
                .map(UserDetailsImpl::getEmail);
    }
}