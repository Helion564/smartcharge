package com.smartcharge.booking.dto;

import lombok.*;

import java.util.Map;

/**
 * Generic Kafka event wrapper used for all booking-service events.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {

    private String eventType;
    private String bookingId;
    private String userId;
    private String stationId;
    private String connectorId;
    private String status;
    private long timestamp;
    private Map<String, Object> payload;

    public static BookingEvent of(String eventType, String bookingId, String userId,
                                   String stationId, String connectorId, String status,
                                   Map<String, Object> payload) {
        return BookingEvent.builder()
                .eventType(eventType)
                .bookingId(bookingId)
                .userId(userId)
                .stationId(stationId)
                .connectorId(connectorId)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();
    }
}
