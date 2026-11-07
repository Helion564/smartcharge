package com.smartcharge.station.service;

import com.smartcharge.station.dto.*;
import com.smartcharge.station.entity.*;
import com.smartcharge.station.event.ConnectorStatusEvent;
import com.smartcharge.station.exception.StationNotFoundException;
import com.smartcharge.station.exception.UnauthorizedOperationException;
import com.smartcharge.station.repository.ConnectorRepository;
import com.smartcharge.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final ConnectorRepository connectorRepository;
    private final AvailabilityService availabilityService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CONNECTOR_TOPIC = "connector-events";

    @Transactional
    public StationResponse createStation(StationRequest request, String operatorId) {
        ChargingStation station = ChargingStation.builder()
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .operatorId(operatorId)
                .operatorName(request.getOperatorName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(StationStatus.ACTIVE)
                .amenities(request.getAmenities())
                .operatingHours(request.getOperatingHours())
                .is24Hours(request.is24Hours())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .website(request.getWebsite())
                .dataSource("MANUAL")
                .build();

        if (request.getConnectors() != null) {
            int seq = 1;
            for (ConnectorRequest cr : request.getConnectors()) {
                String code = String.format("%s-%02d", station.getName().replaceAll("\\s+", "-").toUpperCase(), seq++);
                Connector connector = Connector.builder()
                        .connectorCode(code)
                        .connectorType(cr.getConnectorType())
                        .powerLevel(cr.getPowerLevel())
                        .powerKw(cr.getPowerKw())
                        .pricePerKwh(cr.getPricePerKwh())
                        .pricePerMinute(cr.getPricePerMinute())
                        .status(ConnectorStatus.AVAILABLE)
                        .isAvailable(true)
                        .lastStatusUpdate(LocalDateTime.now())
                        .build();
                station.addConnector(connector);
            }
        }

        ChargingStation saved = stationRepository.save(station);
        return mapToResponse(saved, null);
    }

    @Transactional
    public StationResponse updateStation(UUID stationId, StationRequest request, String operatorId, boolean isAdmin) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException("Station not found: " + stationId));

        if (!isAdmin && !station.getOperatorId().equals(operatorId)) {
            throw new UnauthorizedOperationException("You do not own this station");
        }

        station.setName(request.getName());
        station.setAddressLine1(request.getAddressLine1());
        station.setAddressLine2(request.getAddressLine2());
        station.setCity(request.getCity());
        station.setState(request.getState());
        station.setPincode(request.getPincode());
        station.setOperatorName(request.getOperatorName());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setAmenities(request.getAmenities());
        station.setOperatingHours(request.getOperatingHours());
        station.set24Hours(request.is24Hours());
        station.setContactPhone(request.getContactPhone());
        station.setContactEmail(request.getContactEmail());
        station.setWebsite(request.getWebsite());

        ChargingStation saved = stationRepository.save(station);
        availabilityService.invalidateAvailability(stationId);
        return mapToResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public StationResponse getStationById(UUID stationId) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException("Station not found: " + stationId));
        return mapToResponse(station, null);
    }

    @Transactional(readOnly = true)
    public NearbyStationsResponse searchNearbyStations(StationSearchRequest searchRequest) {
        List<ChargingStation> nearby = stationRepository.findNearbyStations(
                searchRequest.getLatitude(),
                searchRequest.getLongitude(),
                searchRequest.getRadiusKm());

        List<StationResponse> filtered = nearby.stream()
                .filter(station -> {
                    if (searchRequest.getConnectorType() != null) {
                        boolean hasType = station.getConnectors().stream()
                                .anyMatch(c -> c.getConnectorType() == searchRequest.getConnectorType());
                        if (!hasType) return false;
                    }
                    if (searchRequest.getPowerLevel() != null) {
                        boolean hasPower = station.getConnectors().stream()
                                .anyMatch(c -> c.getPowerKw() >= searchRequest.getPowerLevel().getPowerKw());
                        if (!hasPower) return false;
                    }
                    if (searchRequest.getMaxPricePerKwh() != null) {
                        boolean priceOk = station.getConnectors().stream()
                                .anyMatch(c -> c.getPricePerKwh() <= searchRequest.getMaxPricePerKwh());
                        if (!priceOk) return false;
                    }
                    return true;
                })
                .map(station -> {
                    double dist = calculateDistance(
                            searchRequest.getLatitude(), searchRequest.getLongitude(),
                            station.getLatitude(), station.getLongitude());
                    return mapToResponse(station, dist);
                })
                .collect(Collectors.toList());

        long total = filtered.size();
        int page = searchRequest.getPage();
        int size = searchRequest.getSize();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<StationResponse> paged = filtered.subList(fromIndex, toIndex);

        return NearbyStationsResponse.builder()
                .stations(paged)
                .totalCount(total)
                .searchCenterLat(searchRequest.getLatitude())
                .searchCenterLng(searchRequest.getLongitude())
                .radiusKm(searchRequest.getRadiusKm())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getStationsByOperator(String operatorId) {
        return stationRepository.findByOperatorId(operatorId).stream()
                .map(s -> mapToResponse(s, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StationResponse> getAllStations(Pageable pageable) {
        return stationRepository.findAll(pageable)
                .map(s -> mapToResponse(s, null));
    }

    @Transactional
    public void deleteStation(UUID stationId, String operatorId, boolean isAdmin) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException("Station not found: " + stationId));

        if (!isAdmin && !station.getOperatorId().equals(operatorId)) {
            throw new UnauthorizedOperationException("You do not own this station");
        }

        stationRepository.delete(station);
        availabilityService.invalidateAvailability(stationId);
    }

    @Transactional(readOnly = true)
    public StationAvailabilityResponse getStationAvailability(UUID stationId) {
        return availabilityService.getCachedAvailability(stationId)
                .orElseGet(() -> {
                    ChargingStation station = stationRepository.findById(stationId)
                            .orElseThrow(() -> new StationNotFoundException("Station not found: " + stationId));
                    StationAvailabilityResponse resp = availabilityService.buildAvailabilityResponse(station);
                    availabilityService.cacheAvailability(stationId, resp);
                    return resp;
                });
    }

    @Transactional
    public ConnectorResponse updateConnectorStatus(UUID connectorId, ConnectorStatus newStatus) {
        Connector connector = connectorRepository.findById(connectorId)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + connectorId));

        ConnectorStatus oldStatus = connector.getStatus();
        connector.setStatus(newStatus);
        connector.setAvailable(newStatus == ConnectorStatus.AVAILABLE);
        connector.setLastStatusUpdate(LocalDateTime.now());
        Connector updated = connectorRepository.save(connector);

        UUID stationId = connector.getStation().getId();
        availabilityService.invalidateAvailability(stationId);

        // Publish event to Kafka
        try {
            ConnectorStatusEvent event = ConnectorStatusEvent.builder()
                    .stationId(stationId)
                    .connectorId(connectorId)
                    .connectorCode(connector.getConnectorCode())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .timestamp(Instant.now())
                    .build();
            kafkaTemplate.send(CONNECTOR_TOPIC, connectorId.toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish connector status event: {}", e.getMessage());
        }

        return mapToConnectorResponse(updated);
    }

    private StationResponse mapToResponse(ChargingStation station, Double distance) {
        List<ConnectorResponse> connectorResponses = station.getConnectors().stream()
                .map(this::mapToConnectorResponse)
                .collect(Collectors.toList());

        int available = (int) connectorResponses.stream()
                .filter(c -> c.getStatus() == ConnectorStatus.AVAILABLE).count();

        return StationResponse.builder()
                .id(station.getId())
                .ocmId(station.getOcmId())
                .name(station.getName())
                .addressLine1(station.getAddressLine1())
                .addressLine2(station.getAddressLine2())
                .city(station.getCity())
                .state(station.getState())
                .pincode(station.getPincode())
                .operatorId(station.getOperatorId())
                .operatorName(station.getOperatorName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .status(station.getStatus())
                .amenities(station.getAmenities())
                .operatingHours(station.getOperatingHours())
                .is24Hours(station.is24Hours())
                .contactPhone(station.getContactPhone())
                .contactEmail(station.getContactEmail())
                .website(station.getWebsite())
                .dataSource(station.getDataSource())
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt())
                .connectors(connectorResponses)
                .availableConnectors(available)
                .totalConnectors(connectorResponses.size())
                .distanceKm(distance != null ? Math.round(distance * 100.0) / 100.0 : null)
                .build();
    }

    private ConnectorResponse mapToConnectorResponse(Connector c) {
        return ConnectorResponse.builder()
                .id(c.getId())
                .connectorCode(c.getConnectorCode())
                .connectorType(c.getConnectorType())
                .powerLevel(c.getPowerLevel())
                .powerKw(c.getPowerKw())
                .status(c.getStatus())
                .pricePerKwh(c.getPricePerKwh())
                .pricePerMinute(c.getPricePerMinute())
                .isAvailable(c.isAvailable())
                .lastStatusUpdate(c.getLastStatusUpdate())
                .powerLevelDisplayName(c.getPowerLevel() != null ? c.getPowerLevel().getDisplayName() : "")
                .connectorTypeLabel(c.getConnectorType() != null ? c.getConnectorType().name() : "")
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }
}
