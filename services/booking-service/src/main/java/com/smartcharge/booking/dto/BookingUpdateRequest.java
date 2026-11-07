package com.smartcharge.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Request payload for updating booking slot times or estimated energy.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingUpdateRequest {

    @NotNull(message = "New start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "New end time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @Positive(message = "Estimated energy must be positive")
    @Max(value = 200, message = "Estimated energy cannot exceed 200 kWh")
    private double estimatedEnergyKwh;

    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime) && java.time.Duration.between(startTime, endTime).toMinutes() >= 15;
    }
}
