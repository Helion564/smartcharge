package com.smartcharge.station.controller;

import com.smartcharge.station.dto.*;
import com.smartcharge.station.entity.ConnectorStatus;
import com.smartcharge.station.service.OcmImportService;
import com.smartcharge.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final OcmImportService ocmImportService;

    @GetMapping
    public ResponseEntity<Page<StationResponse>> getAllStations(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(stationService.getAllStations(pageable));
    }

    @GetMapping("/nearby")
    public ResponseEntity<NearbyStationsResponse> searchNearby(
            @ModelAttribute StationSearchRequest searchRequest) {
        return ResponseEntity.ok(stationService.searchNearbyStations(searchRequest));
    }

    @GetMapping("/{stationId}")
    public ResponseEntity<StationResponse> getStationById(@PathVariable UUID stationId) {
        return ResponseEntity.ok(stationService.getStationById(stationId));
    }

    @GetMapping("/{stationId}/availability")
    public ResponseEntity<StationAvailabilityResponse> getStationAvailability(@PathVariable UUID stationId) {
        return ResponseEntity.ok(stationService.getStationAvailability(stationId));
    }

    @PostMapping
    public ResponseEntity<StationResponse> createStation(
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
            @Valid @RequestBody StationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stationService.createStation(request, userId));
    }

    @PutMapping("/{stationId}")
    public ResponseEntity<StationResponse> updateStation(
            @PathVariable UUID stationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody StationRequest request) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(stationService.updateStation(stationId, request, userId, isAdmin));
    }

    @DeleteMapping("/{stationId}")
    public ResponseEntity<Void> deleteStation(
            @PathVariable UUID stationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        stationService.deleteStation(stationId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/connectors/{connectorId}/status")
    public ResponseEntity<ConnectorResponse> updateConnectorStatus(
            @PathVariable UUID connectorId,
            @RequestParam ConnectorStatus status) {
        return ResponseEntity.ok(stationService.updateConnectorStatus(connectorId, status));
    }

    @PostMapping("/import/ocm")
    public ResponseEntity<OcmImportResponse> triggerOcmImport(
            @RequestParam(defaultValue = "50") int maxResults) {
        return ResponseEntity.ok(ocmImportService.importIndiaStations(maxResults));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<StationResponse>> getStationsByOperator(@PathVariable String operatorId) {
        return ResponseEntity.ok(stationService.getStationsByOperator(operatorId));
    }
}
