package com.smartcharge.station.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartcharge.station.entity.StationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a charging station including its connectors.
 * {@code distanceKm} is populated only in nearby-search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StationResponse {

    private UUID id;
    private String ocmId;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String operatorId;
    private String operatorName;
    private double latitude;
    private double longitude;
    private StationStatus status;

    /** JSON array string of amenity tags */
    private String amenities;
    private String operatingHours;
    private boolean is24Hours;
    private String contactPhone;
    private String contactEmail;
    private String website;
    private String dataSource;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ConnectorResponse> connectors;

    /** Number of connectors currently in AVAILABLE state */
    private int availableConnectors;

    /** Total number of connectors at this station */
    private int totalConnectors;

    /**
     * Straight-line distance from the search centre in km.
     * Null for non-nearby queries.
     */
    private Double distanceKm;
}
