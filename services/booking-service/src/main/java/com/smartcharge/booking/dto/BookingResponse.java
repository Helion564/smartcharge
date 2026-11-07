package com.smartcharge.booking.dto;

import com.smartcharge.booking.enums.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload carrying booking details enriched with station and connector information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private UUID id;
    private String userId;
    private String stationId;
    private String connectorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private double estimatedEnergyKwh;
    private double actualEnergyKwh;
    private double estimatedCost;
    private double actualCost;
    private double pricePerKwh;
    private BookingStatus status;
    private String vehicleModel;
    private String vehicleRegistration;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private String paymentId;

    // Enriched fields from station-service
    private String stationName;
    private String stationAddress;
    private String connectorType;
    private double connectorMaxPowerKw;

    /** Duration of the booked slot in minutes. */
    public long getBookedDurationMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }
}
