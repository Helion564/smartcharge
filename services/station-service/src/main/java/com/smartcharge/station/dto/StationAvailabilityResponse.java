package com.smartcharge.station.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationAvailabilityResponse {
    private UUID stationId;
    private int totalConnectors;
    private int availableConnectors;
    private List<ConnectorResponse> connectors;
}
