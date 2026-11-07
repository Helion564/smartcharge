package com.smartcharge.station.dto;

import com.smartcharge.station.entity.ConnectorType;
import com.smartcharge.station.entity.PowerLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding or updating a connector on a station.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorRequest {

    @NotNull(message = "Connector type is required")
    private ConnectorType connectorType;

    @NotNull(message = "Power level is required")
    private PowerLevel powerLevel;

    @NotNull(message = "Actual power (kW) is required")
    @DecimalMin(value = "0.5", message = "Power must be at least 0.5 kW")
    @DecimalMax(value = "500.0", message = "Power cannot exceed 500 kW")
    private Double powerKw;

    @NotNull(message = "Price per kWh is required")
    @DecimalMin(value = "0.0", message = "Price per kWh cannot be negative")
    @DecimalMax(value = "100.0", message = "Price per kWh seems unrealistically high")
    private Double pricePerKwh;

    @DecimalMin(value = "0.0", message = "Price per minute cannot be negative")
    @Builder.Default
    private double pricePerMinute = 0.0;
}
