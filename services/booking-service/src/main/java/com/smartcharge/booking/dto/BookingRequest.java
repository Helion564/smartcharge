package com.smartcharge.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Request payload for creating a new booking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotBlank(message = "Station ID is required")
    private String stationId;

    @NotBlank(message = "Connector ID is required")
    private String connectorId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @Positive(message = "Estimated energy must be positive")
    @Max(value = 200, message = "Estimated energy cannot exceed 200 kWh")
    private double estimatedEnergyKwh;

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 100, message = "Vehicle model cannot exceed 100 characters")
    private String vehicleModel;

    @NotBlank(message = "Vehicle registration is required")
    @Size(max = 20, message = "Vehicle registration cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Invalid vehicle registration format")
    private String vehicleRegistration;

    /**
     * Custom validation logic (invoked in service layer).
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime) && java.time.Duration.between(startTime, endTime).toMinutes() >= 15;
    }
}
