package com.ali.loomabackend.model.dto.response.user;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private UUID userId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String bio;

    private String profilePictureUrl;

    private LocalDate dateOfBirth;

    private String gender;

    private String website;

    private String twitterHandle;

    private String linkedinUrl;

    private String githubUsername;

}
