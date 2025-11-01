package com.ali.loomabackend.model.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
    private Long followersCount;
    private Long followingCount;
    private Boolean emailVerified;

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return displayName != null ? displayName : username;
        }
        return String.format("%s %s",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "").trim();
    }
}