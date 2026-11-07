package com.smartcharge.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent {
    private String eventType;
    private UUID sessionId;
    private String userId;
    private String stationId;
    private String connectorId;
    private String bookingId;
    private double energyConsumedKwh;
    private double totalCost;
    private Instant timestamp;
}

