package com.ali.loomabackend.model.dto.response.auth;

import com.ali.loomabackend.model.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;

    private UUID id;
    private String username;
    private String email;
    private List<Role> roles;
}
