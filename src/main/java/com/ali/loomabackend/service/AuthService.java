package com.ali.loomabackend.service;

import com.ali.loomabackend.event.UserEvents;
import com.ali.loomabackend.exception.custom.*;
import com.ali.loomabackend.model.dto.request.auth.FinalRegisterRequest;
import com.ali.loomabackend.model.dto.request.auth.InitialRegisterRequest;
import com.ali.loomabackend.model.dto.request.auth.LoginRequest;
import com.ali.loomabackend.model.dto.request.auth.VerifyEmailRequest;
import com.ali.loomabackend.model.dto.response.auth.*;
import com.ali.loomabackend.model.entity.user.*;
import com.ali.loomabackend.model.enums.ErrorCode;
import com.ali.loomabackend.model.enums.user.UserRolesEnum;
import com.ali.loomabackend.model.enums.user.UserStatus;
import com.ali.loomabackend.repository.user.*;
import com.ali.loomabackend.security.UserDetailsImpl;
import com.ali.loomabackend.security.jwt.JwtTokenProvider;
import com.ali.loomabackend.service.email.BrevoEmailService;
import com.ali.loomabackend.service.email.EmailService;
import com.ali.loomabackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TempUserRepository tempUserRepository;
    private final BrevoEmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final SecurityUtils securityUtils;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public CheckEmailUsernameResponse isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        boolean isAvailable = !userRepository.existsByUsername(username.trim());
        return new CheckEmailUsernameResponse(isAvailable);
    }

    @Transactional(readOnly = true)
    public CheckEmailUsernameResponse isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        boolean isAvailable = !userRepository.existsByEmail(email.toLowerCase().trim());
        return new CheckEmailUsernameResponse(isAvailable);
    }

    @Transactional
    public InitialRegisterResponse registerInitialUser(InitialRegisterRequest initialRegisterRequest) {
        if(userRepository.existsByEmail(initialRegisterRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if(tempUserRepository.existsByEmail(initialRegisterRequest.getEmail())){
            tempUserRepository.deleteByEmail(initialRegisterRequest.getEmail());
        }

        if( Period.between(initialRegisterRequest.getDateOfBirth(), LocalDate.now()).getYears() < 13 ) {
            throw new IllegalArgumentException("You must be at least 13 years old to register.");
        }

        Random random = new SecureRandom();
        int verificationCode = 100000 + random.nextInt(900000);

        TempUser tempUser = TempUser.builder()
                .name(initialRegisterRequest.getName())
                .email(initialRegisterRequest.getEmail())
                .verificationCode(verificationCode)
                .expiryDate(LocalDateTime.now().plusMinutes(15))  // verificationCode Valid for 15 min
                .isUsed(false)
                .dateOfBirth(initialRegisterRequest.getDateOfBirth())
                .build();
        tempUserRepository.save(tempUser);

        emailService.sendVerificationEmail(tempUser.getEmail(), String.valueOf(verificationCode));

        String token = jwtTokenProvider.generateAccessTokenForTempUser(tempUser);

        return new InitialRegisterResponse(token, true);
    }

    @Transactional
    public ResendVerificationEmailResponse resendVerificationEmail(String email) {
        Random random = new SecureRandom();
        int verificationCode = 100000 + random.nextInt(900000);

        TempUser tempUser = tempUserRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Email not found")
        );

        if(Boolean.TRUE.equals(tempUser.getIsUsed())) {
            throw new IllegalArgumentException("Email has already been verified.");
        }

        tempUser.setVerificationCode(verificationCode);
        tempUser.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tempUserRepository.save(tempUser);

        emailService.sendVerificationEmail(tempUser.getEmail(), String.valueOf(verificationCode));

        String token = jwtTokenProvider.generateAccessTokenForTempUser(tempUser);

        return ResendVerificationEmailResponse.builder()
                .sent(true)
                .accessToken(token)
                .build();
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest verifyEmailRequest, String email) {
        TempUser tempUser = tempUserRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Email not found")
        );

        if(tempUser.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
        if(Boolean.TRUE.equals(tempUser.getIsUsed())) {
            throw new IllegalArgumentException("Verification code has already been used.");
        }

        if(tempUser.getVerificationCode() == verifyEmailRequest.getVerificationCode()) {
            tempUser.setVerified(true);
            tempUser.setIsUsed(true);
            tempUserRepository.save(tempUser);

            return new VerifyEmailResponse(true);
        }

        // If code is wrong
        throw new IllegalArgumentException("Invalid verification code.");
    }

    @Transactional
    public AuthResponse registerUser(FinalRegisterRequest registerRequest, String email) {
        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if(userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        TempUser tempUser = tempUserRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email not found")
        );

        if(Boolean.FALSE.equals(tempUser.getVerified())) {
            throw new IllegalArgumentException("Email not verified. Please verify your email first.");
        }

        Role role = roleRepository.findByRoleName(UserRolesEnum.USER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role 'USER' not found. System configuration error."));

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(tempUser.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .emailVerified(tempUser.getVerified())
                .build();

        User savedUser = userRepository.save(user);

        UserRole userRole = UserRole.builder()
                .userId(savedUser.getId())
                .roleId(role.getId())
                .build();

        UserRole savedUserRole = userRoleRepository.save(userRole);

        List<UUID> userRoles = userRoleRepository.findRoleIdsByUserId(savedUserRole.getUserId());

        List<Role> savedRoles = roleRepository.findAllByIdIn(userRoles);


        UserProfile userProfile = UserProfile.builder()
                .userId(savedUser.getId())
                .firstName(tempUser.getName())
                .dateOfBirth(tempUser.getDateOfBirth())
                .build();

        userProfileRepository.save(userProfile);

        tempUserRepository.delete(tempUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(SecurityContextHolder.getContext().getAuthentication());

        // 🔥 Publish event to index in Elasticsearch
        applicationEventPublisher.publishEvent(new UserEvents.UserCreatedEvent(user.getId()));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedRoles)
                .build();
    }


    @Transactional
    public AuthResponse loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        if(!user.isEmailVerified()){
            throw new AuthorizationException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if(user.getDeleted() != null && user.getDeleted()){
            throw new AccountDeletedException();
        }

        if(user.getStatus() == UserStatus.BANNED){
            throw new AccountBannedException();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User details not found after authentication.")
        );

        List<UUID> userRoles = userRoleRepository.findRoleIdsByUserId(userDetails.getUserId());

        List<Role> roles = roleRepository.findAllByIdIn(userRoles);


        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String token) {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(token);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for the provided refresh token."));

        List<UUID> userRoleIds = userRoleRepository.findRoleIdsByUserId(user.getId());

        List<Role> roles = roleRepository.findAllByIdIn(userRoleIds);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        UserDetailsImpl userDetails = new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStatus(),
                user.getDeleted(),
                user.isEmailVerified(),
                authorities);

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    public LogoutResponse logoutUser(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }
        return new LogoutResponse(true);
    }

    public LogoutResponse logoutFromAllDevices() {
        UserDetailsImpl userDetails = securityUtils.getCurrentUserDetails().orElseThrow(
                () -> new ResourceNotFoundException("User not found. Cannot log out.")
        );
        refreshTokenService.revokeAllUserTokens(userDetails.getUserId());
        refreshTokenService.deleteAllUserTokens(userDetails.getUserId());
        return new LogoutResponse(true);
    }
}
