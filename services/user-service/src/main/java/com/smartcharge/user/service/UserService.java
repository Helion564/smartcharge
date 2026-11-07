package com.smartcharge.user.service;

import com.smartcharge.user.dto.UpdateProfileRequest;
import com.smartcharge.user.dto.UserProfileResponse;
import com.smartcharge.user.entity.User;
import com.smartcharge.user.event.UserEvent;
import com.smartcharge.user.exception.ResourceNotFoundException;
import com.smartcharge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_EVENTS_TOPIC = "user-events";

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = findUserById(userId);
        return mapToProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        publishUserEvent(UserEvent.USER_UPDATED, updatedUser);

        return mapToProfileResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToProfileResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUsersByRole(User.Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable).map(this::mapToProfileResponse);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = findUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);

        publishUserEvent(UserEvent.USER_DEACTIVATED, user);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
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

