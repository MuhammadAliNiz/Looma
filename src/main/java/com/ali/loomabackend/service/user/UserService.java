package com.ali.loomabackend.service.user;

import com.ali.loomabackend.exception.custom.ResourceNotFoundException;
import com.ali.loomabackend.model.dto.response.user.ProfilePicResponse;
import com.ali.loomabackend.model.dto.response.user.ProfileResponse;
import com.ali.loomabackend.model.entity.user.UserProfile;
import com.ali.loomabackend.repository.user.UserProfileRepository;
import com.ali.loomabackend.security.UserDetailsImpl;
import com.ali.loomabackend.service.S3Service;
import com.ali.loomabackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserProfileRepository userProfileRepository;
    private final SecurityUtils securityUtils;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public ProfileResponse getUserProfile() {
        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User not found. Please log in.")
        );

        UserProfile userProfile = userProfileRepository.findByUserId(userDetails.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User profile not found for user ID: " + userDetails.getUserId())
        );

        return ProfileResponse.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getActualUsername())
                .email(userDetails.getEmail())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .bio(userProfile.getBio())
                .profilePictureUrl(s3Service.getFileUrl(userProfile.getProfilePictureUrl()))
                .dateOfBirth(userProfile.getDateOfBirth())
                .gender(userProfile.getGender() != null ? userProfile.getGender().name() : null)
                .githubUsername(userProfile.getGithubUsername())
                .linkedinUrl(userProfile.getLinkedinUrl())
                .twitterHandle(userProfile.getTwitterHandle())
                .website(userProfile.getWebsite())
                .build();
    }

    @Transactional
    public ProfilePicResponse uploadProfilePic(MultipartFile profilePic) throws IOException {
        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User not found. Please log in.")
        );

        String s3Key = s3Service.uploadFile(profilePic, "images/profilePic", userDetails.getUserId().toString());

        UserProfile userProfile = userProfileRepository.findByUserId(userDetails.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User profile not found for user ID: " + userDetails.getUserId())
        );

        String oldS3Key = userProfile.getProfilePictureUrl();
        if (oldS3Key != null && !oldS3Key.isEmpty()) {
            try {

                s3Service.deleteFile(oldS3Key);
            } catch (RuntimeException e) { // Changed from BusinessException to RuntimeException
                // Log the error but don't fail the upload. Maybe the old file was already deleted.
                // You might want a queue for failed deletions.
                log.error("Failed to delete old profile picture: {} | Error: {}", oldS3Key, e.getMessage());
            }
        }

        userProfile.setProfilePictureUrl(s3Key);
        userProfileRepository.save(userProfile);

        String s3Url = s3Service.getFileUrl(s3Key);

        return ProfilePicResponse.builder().profilePictureUrl(s3Url).build();
    }
}

