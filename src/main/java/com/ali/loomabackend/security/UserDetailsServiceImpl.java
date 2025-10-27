package com.ali.loomabackend.security;
import com.ali.loomabackend.model.entity.user.Role;
import com.ali.loomabackend.model.entity.user.User;
import com.ali.loomabackend.repository.user.RoleRepository;
import com.ali.loomabackend.repository.user.UserRepository;
import com.ali.loomabackend.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username/email: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        List<UUID> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        List<Role> roles = roleIds.isEmpty() ? List.of() :
                roleRepository.findAllByIdIn(roleIds);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .toList();

        log.debug("Loaded user {} with {} roles", user.getUsername(), authorities.size());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStatus(),
                user.getDeleted(),
                user.getEmailVerified(),
                authorities
        );
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID userId) {
        log.debug("Loading user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));

        List<UUID> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        List<Role> roles = roleIds.isEmpty() ? List.of() :
                roleRepository.findAllByIdIn(roleIds);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .toList();

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStatus(),
                user.getDeleted(),
                user.getEmailVerified(),
                authorities
        );
    }
}
