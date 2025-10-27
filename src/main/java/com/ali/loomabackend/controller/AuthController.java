package com.ali.loomabackend.controller;

import com.ali.loomabackend.exception.custom.ResourceNotFoundException;
import com.ali.loomabackend.model.dto.request.auth.FinalRegisterRequest;
import com.ali.loomabackend.model.dto.request.auth.InitialRegisterRequest;
import com.ali.loomabackend.model.dto.request.auth.LoginRequest;
import com.ali.loomabackend.model.dto.request.auth.VerifyEmailRequest;
import com.ali.loomabackend.model.dto.response.ApiResponse;
import com.ali.loomabackend.model.dto.response.auth.*;
import com.ali.loomabackend.model.entity.user.RefreshToken;
import com.ali.loomabackend.security.UserDetailsImpl;
import com.ali.loomabackend.security.jwt.JwtTokenProvider;
import com.ali.loomabackend.service.AuthService;
import com.ali.loomabackend.service.RefreshTokenService;
import com.ali.loomabackend.util.CookieUtils;
import com.ali.loomabackend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;
    private final SecurityUtils securityUtils;

    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<CheckEmailUsernameResponse>> checkUsernameAvailability(@RequestParam String username) {
        CheckEmailUsernameResponse responseData = authService.isUsernameAvailable(username);
        ApiResponse<CheckEmailUsernameResponse> apiResponse = ApiResponse.success(responseData, "Username availability checked");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<CheckEmailUsernameResponse>> checkEmailAvailability(@RequestParam String email) {
        CheckEmailUsernameResponse responseData = authService.isEmailAvailable(email);
        ApiResponse<CheckEmailUsernameResponse> apiResponse = ApiResponse.success(responseData, "Email availability checked");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/initial-register")
    public ResponseEntity<ApiResponse<InitialRegisterResponse>> initialRegister(@Validated @RequestBody InitialRegisterRequest initialRegisterRequest) {
        InitialRegisterResponse responseData = authService.registerInitialUser(initialRegisterRequest);
        ApiResponse<InitialRegisterResponse> apiResponse = ApiResponse.success(responseData, "Initial registration successful. Please check your email.");
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<ApiResponse<ResendVerificationEmailResponse>> resendVerificationEmail(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        String token;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else {
            throw new IllegalArgumentException("A valid 'Bearer' authorization header is required");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        ResendVerificationEmailResponse response = authService.resendVerificationEmail(email);
        ApiResponse<ResendVerificationEmailResponse> apiResponse = ApiResponse.success(response, "Verification email resent successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyUser(
            @Validated @RequestBody VerifyEmailRequest verifyEmailRequest,
            HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        String token;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else {
            throw new IllegalArgumentException("A valid 'Bearer' authorization header is required");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        VerifyEmailResponse verifyEmailResponse = authService.verifyEmail(verifyEmailRequest, email);
        ApiResponse<VerifyEmailResponse> apiResponse = ApiResponse.success(verifyEmailResponse, "Email verified successfully.");
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/final-register")
    public ResponseEntity<ApiResponse<AuthResponse>> finalRegister(
            @Validated @RequestBody FinalRegisterRequest registerRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String bearerToken = request.getHeader("Authorization");
        String token;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else {
            throw new IllegalArgumentException("A valid 'Bearer' authorization header is required");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        AuthResponse authResponse = authService.registerUser(registerRequest, email);

        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User not found after login")
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails, request);

        ResponseCookie refreshTokenCookie = cookieUtils.createHttpOnlyCookie(
                "refreshToken",
                refreshToken.getToken(),
                7 * 24 * 60 * 60L // 7 days
        );
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authResponse, "User registered successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Validated @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        AuthResponse loginResponse = authService.loginUser(loginRequest);


        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User not found after login")
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails, request);

        ResponseCookie refreshTokenCookie = cookieUtils.createHttpOnlyCookie(
                "refreshToken",
                refreshToken.getToken(),
                7 * 24 * 60 * 60L // 7 days
        );
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(loginResponse, "Login successful.");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = cookieUtils.extractCookieValue(request, "refreshToken");
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authResponse, "Token refreshed successfully.");
        return ResponseEntity.ok(apiResponse);
    }
}
