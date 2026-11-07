package com.smartcharge.session.dto;

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
public class ActiveSessionResponse {
    private UUID sessionId;
    private String userId;
    private String stationId;
    private String connectorId;
    private LocalDateTime startTime;
    private double currentEnergyKwh;
    private double currentPowerKw;
    private double currentCost;
    private double stateOfCharge;
    private long durationMinutes;
}
