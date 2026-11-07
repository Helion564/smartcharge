package com.smartcharge.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummary {
    private String userId;
    private long unreadCount;
    private List<NotificationResponse> recentNotifications;
}

