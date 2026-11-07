package com.smartcharge.station.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartcharge.station.entity.ConnectorStatus;
import com.smartcharge.station.entity.ConnectorType;
import com.smartcharge.station.entity.PowerLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a single connector.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectorResponse {

    private UUID id;
    private String connectorCode;
    private ConnectorType connectorType;
    private PowerLevel powerLevel;
    private double powerKw;
    private ConnectorStatus status;
    private double pricePerKwh;
    private double pricePerMinute;
    private boolean isAvailable;
    private LocalDateTime lastStatusUpdate;

    /** Display name of the power level, e.g. "DC Fast (50kW)" */
    private String powerLevelDisplayName;

    /** Localised connector type label for UI */
    private String connectorTypeLabel;
}
