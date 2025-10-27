package com.ali.loomabackend.security;


import com.ali.loomabackend.model.entity.user.User;
import com.ali.loomabackend.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * ⚠️ CRITICAL CHANGE: Do NOT store User entity in Security Context
 * Only store essential data needed for authentication
 */
@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String email;
    private final String password;
    private final UserStatus status;
    private final Boolean deleted;
    private final Boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Using email as username
    }

    public String getActualUsername() {
        return username; // Get the actual username field
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED && status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && !deleted;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && !deleted;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }
}