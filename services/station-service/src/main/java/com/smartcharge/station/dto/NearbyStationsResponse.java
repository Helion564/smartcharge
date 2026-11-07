package com.smartcharge.station.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for nearby station search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyStationsResponse {

    private List<StationResponse> stations;

    /** Total stations found within the radius (before pagination) */
    private long totalCount;

    /** Search centre latitude */
    private double searchCenterLat;

    /** Search centre longitude */
    private double searchCenterLng;

    /** Radius used for the search, in km */
    private double radiusKm;

    /** Current page index (zero-based) */
    private int page;

    /** Page size */
    private int size;
}
