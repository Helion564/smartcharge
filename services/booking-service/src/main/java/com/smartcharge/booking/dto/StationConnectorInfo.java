package com.smartcharge.booking.dto;

import lombok.*;

/**
 * Payload received from station-service when enriching a booking response.
 * Fields are nullable — service handles partial failures gracefully.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationConnectorInfo {

    private String stationId;
    private String stationName;
    private String stationAddress;
    private String connectorId;
    private String connectorType;
    private double maxPowerKw;
    private double pricePerKwh;
    private String status;
}
