package com.smartcharge.session.dto;

import com.smartcharge.session.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private UUID id;
    private String userId;
    private String stationId;
    private String connectorId;
    private String bookingId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double startEnergyKwh;
    private double endEnergyKwh;
    private double energyConsumedKwh;
    private double powerKw;
    private SessionStatus status;
    private double totalCost;
    private double pricePerKwh;
    private String vehicleModel;
    private String vehicleRegistration;
    private String stopReason;
    private Long durationMinutes;
    private List<SessionDataPointResponse> recentDataPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDataPointResponse {
        private LocalDateTime recordedAt;
        private double currentPowerKw;
        private double energyKwh;
        private double stateOfCharge;
        private double currentAmps;
        private double voltageVolts;
        private double temperature;
    }
}

