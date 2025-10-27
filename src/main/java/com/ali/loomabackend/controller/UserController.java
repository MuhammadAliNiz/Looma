package com.ali.loomabackend.controller;

import com.ali.loomabackend.model.dto.response.ApiResponse;
import com.ali.loomabackend.model.dto.response.auth.LogoutResponse;
import com.ali.loomabackend.model.dto.response.user.ProfilePicResponse;
import com.ali.loomabackend.model.dto.response.user.ProfileResponse;
import com.ali.loomabackend.service.AuthService;
import com.ali.loomabackend.service.user.UserService;
import com.ali.loomabackend.util.CookieUtils;
import com.ali.loomabackend.util.FileValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;
    private final UserService userService;
    private final FileValidator fileValidator;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.extractCookieValue(request, "refreshToken");
        LogoutResponse logoutResponse = authService.logoutUser(refreshToken);

        response.addHeader("Set-Cookie", cookieUtils.deleteHttpOnlyCookie("refreshToken").toString());

        ApiResponse<LogoutResponse> apiResponse = ApiResponse.success(logoutResponse, "Logged out successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<LogoutResponse>> logoutAll(HttpServletResponse response) {
        LogoutResponse logoutResponse = authService.logoutFromAllDevices();

        response.addHeader("Set-Cookie", cookieUtils.deleteHttpOnlyCookie("refreshToken").toString());

        ApiResponse<LogoutResponse> apiResponse = ApiResponse.success(logoutResponse, "Logged out from all devices successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        ProfileResponse profileResponse = userService.getUserProfile();
        ApiResponse<ProfileResponse> apiResponse = ApiResponse.success(profileResponse, "Profile fetched successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/upload-profile-pic")
    public ResponseEntity<ApiResponse<ProfilePicResponse>> uploadProfilePic(@RequestParam("profilePic") MultipartFile profilePic) throws IOException {
        boolean isValidPic = fileValidator.isSupportedImageType(profilePic);

        if(!isValidPic) {
            throw new IllegalArgumentException("Invalid image type. Supported types are PNG, JPG, JPEG, WEBP.");
        }

        ProfilePicResponse profilePicResponse = userService.uploadProfilePic(profilePic);
        ApiResponse<ProfilePicResponse> apiResponse = ApiResponse.success(profilePicResponse, "Profile picture uploaded successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
