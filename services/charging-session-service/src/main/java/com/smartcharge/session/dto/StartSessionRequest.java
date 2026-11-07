package com.smartcharge.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionRequest {

    @NotBlank(message = "Station ID is required")
    private String stationId;

    @NotBlank(message = "Connector ID is required")
    private String connectorId;

    private String bookingId;
    private String vehicleModel;
    private String vehicleRegistration;
    private Double pricePerKwh;
}
