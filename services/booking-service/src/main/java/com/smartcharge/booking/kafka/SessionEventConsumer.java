package com.smartcharge.booking.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcharge.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventConsumer {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "session-events", groupId = "booking-service-group")
    public void handleSessionEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();

            if ("SESSION_COMPLETED".equalsIgnoreCase(eventType)) {
                String bookingIdStr = node.path("bookingId").asText(null);
                if (bookingIdStr != null && !bookingIdStr.isBlank() && !bookingIdStr.equals("null")) {
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    double energy = node.path("energyConsumedKwh").asDouble(0.0);
                    double cost = node.path("totalCost").asDouble(0.0);
                    log.info("Completing booking {} from session event with energy={} kWh, cost=INR {}", bookingId, energy, cost);
                    bookingService.completeBooking(bookingId, energy, cost);
                }
            }
        } catch (Exception e) {
            log.error("Error processing session event in booking service: {}", e.getMessage());
        }
    }
}

