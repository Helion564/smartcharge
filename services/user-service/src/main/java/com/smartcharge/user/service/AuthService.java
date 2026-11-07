package com.smartcharge.user.service;

import com.smartcharge.user.dto.*;
import com.smartcharge.user.entity.RefreshToken;
import com.smartcharge.user.entity.User;
import com.smartcharge.user.event.UserEvent;
import com.smartcharge.user.exception.DuplicateEmailException;
import com.smartcharge.user.exception.InvalidTokenException;
import com.smartcharge.user.exception.ResourceNotFoundException;
import com.smartcharge.user.repository.RefreshTokenRepository;
import com.smartcharge.user.repository.UserRepository;
import com.smartcharge.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private static final String USER_EVENTS_TOPIC = "user-events";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.Role.EV_OWNER)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        // Publish event to Kafka
        publishUserEvent(UserEvent.USER_REGISTERED, savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        publishUserEvent(UserEvent.USER_LOGGED_IN, user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtTokenProvider.generateRefreshToken())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private void publishUserEvent(String eventType, User user) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventType(eventType)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .timestamp(Instant.now())
                    .build();
            kafkaTemplate.send(USER_EVENTS_TOPIC, user.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for user {}: {}", user.getId(), e.getMessage());
        }
    }
}
