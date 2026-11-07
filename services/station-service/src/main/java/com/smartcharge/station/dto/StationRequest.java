package com.smartcharge.station.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a charging station.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationRequest {

    @NotBlank(message = "Station name is required")
    @Size(min = 3, max = 255, message = "Station name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 500)
    private String addressLine1;

    @Size(max = 500)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian pincode format")
    private String pincode;

    @NotBlank(message = "Operator name is required")
    @Size(max = 255)
    private String operatorName;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    /** JSON array string, e.g. ["WIFI","RESTROOM"] */
    private String amenities;

    @Size(max = 255)
    private String operatingHours;

    private boolean is24Hours;

    @Pattern(regexp = "^[+]?[0-9\\-\\s]{6,20}$", message = "Invalid phone number format")
    private String contactPhone;

    @Email(message = "Invalid contact email")
    private String contactEmail;

    @Size(max = 500)
    private String website;

    @Valid
    private List<ConnectorRequest> connectors;
}
