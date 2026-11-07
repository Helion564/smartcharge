package com.smartcharge.notification.dto;

import com.smartcharge.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
