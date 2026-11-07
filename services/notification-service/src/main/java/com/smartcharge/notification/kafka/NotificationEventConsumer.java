package com.smartcharge.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcharge.notification.enums.NotificationType;
import com.smartcharge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void handleBookingEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();
            String userId = node.path("userId").asText();
            String bookingId = node.path("bookingId").asText();

            if ("BOOKING_CREATED".equalsIgnoreCase(eventType)) {
                notificationService.createNotification(
                        userId,
                        NotificationType.BOOKING_CONFIRMED,
                        "Booking Confirmed!",
                        "Your charging slot booking (ID: " + bookingId + ") has been confirmed.",
                        bookingId
                );
            } else if ("BOOKING_CANCELLED".equalsIgnoreCase(eventType)) {
                notificationService.createNotification(
                        userId,
                        NotificationType.BOOKING_CANCELLED,
                        "Booking Cancelled",
                        "Your booking (ID: " + bookingId + ") has been cancelled.",
                        bookingId
                );
            }
        } catch (Exception e) {
            log.error("Failed to parse booking event in notification service: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "session-events", groupId = "notification-service-group")
    public void handleSessionEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();
            String userId = node.path("userId").asText();
            String sessionId = node.path("sessionId").asText();

            if ("SESSION_STARTED".equalsIgnoreCase(eventType)) {
                notificationService.createNotification(
                        userId,
                        NotificationType.SESSION_STARTED,
                        "Charging Started",
                        "Your EV charging session has begun.",
                        sessionId
                );
            } else if ("SESSION_COMPLETED".equalsIgnoreCase(eventType)) {
                double energy = node.path("energyConsumedKwh").asDouble(0.0);
                double cost = node.path("totalCost").asDouble(0.0);
                notificationService.createNotification(
                        userId,
                        NotificationType.SESSION_COMPLETED,
                        "Charging Completed",
                        String.format("Session finished! Consumed %.2f kWh, Total: INR %.2f", energy, cost),
                        sessionId
                );
            }
        } catch (Exception e) {
            log.error("Failed to parse session event in notification service: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();
            String userId = node.path("userId").asText();
            String paymentId = node.path("paymentId").asText();
            double amount = node.path("amount").asDouble(0.0);

            if ("PAYMENT_SUCCESS".equalsIgnoreCase(eventType)) {
                notificationService.createNotification(
                        userId,
                        NotificationType.PAYMENT_SUCCESS,
                        "Payment Successful",
                        String.format("Payment of INR %.2f was processed successfully.", amount),
                        paymentId
                );
            } else if ("PAYMENT_FAILED".equalsIgnoreCase(eventType)) {
                notificationService.createNotification(
                        userId,
                        NotificationType.PAYMENT_FAILED,
                        "Payment Failed",
                        "A payment transaction could not be completed. Please check your balance.",
                        paymentId
                );
            }
        } catch (Exception e) {
            log.error("Failed to parse payment event in notification service: {}", e.getMessage());
        }
    }
}
