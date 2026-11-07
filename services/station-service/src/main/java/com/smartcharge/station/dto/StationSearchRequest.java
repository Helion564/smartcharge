package com.smartcharge.station.dto;

import com.smartcharge.station.entity.ConnectorType;
import com.smartcharge.station.entity.PowerLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for searching nearby charging stations.
 * All fields except lat/lng are optional filters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationSearchRequest {

    /** Search centre latitude */
    private double latitude;

    /** Search centre longitude */
    private double longitude;

    /** Search radius in kilometres (default: 10 km) */
    @Builder.Default
    private double radiusKm = 10.0;

    /** Optional: filter by specific connector type */
    private ConnectorType connectorType;

    /** Optional: filter by minimum power level */
    private PowerLevel powerLevel;

    /** Optional: filter by maximum price per kWh (INR) */
    private Double maxPricePerKwh;

    /** Pagination – zero-based page index */
    @Builder.Default
    private int page = 0;

    /** Pagination – number of results per page */
    @Builder.Default
    private int size = 20;
}
