package com.smartcharge.station.service;

import com.smartcharge.station.dto.ConnectorResponse;
import com.smartcharge.station.dto.StationAvailabilityResponse;
import com.smartcharge.station.entity.ChargingStation;
import com.smartcharge.station.entity.ConnectorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String AVAILABILITY_KEY_PREFIX = "station:availability:";
    private static final Duration TTL = Duration.ofSeconds(60);

    public Optional<StationAvailabilityResponse> getCachedAvailability(UUID stationId) {
        try {
            Object cached = redisTemplate.opsForValue().get(AVAILABILITY_KEY_PREFIX + stationId);
            if (cached instanceof StationAvailabilityResponse resp) {
                return Optional.of(resp);
            }
        } catch (Exception e) {
            log.warn("Redis read error for station {}: {}", stationId, e.getMessage());
        }
        return Optional.empty();
    }

    public void cacheAvailability(UUID stationId, StationAvailabilityResponse response) {
        try {
            redisTemplate.opsForValue().set(AVAILABILITY_KEY_PREFIX + stationId, response, TTL);
        } catch (Exception e) {
            log.warn("Redis write error for station {}: {}", stationId, e.getMessage());
        }
    }

    public void invalidateAvailability(UUID stationId) {
        try {
            redisTemplate.delete(AVAILABILITY_KEY_PREFIX + stationId);
        } catch (Exception e) {
            log.warn("Redis delete error for station {}: {}", stationId, e.getMessage());
        }
    }

    public StationAvailabilityResponse buildAvailabilityResponse(ChargingStation station) {
        List<ConnectorResponse> connectorResponses = station.getConnectors().stream()
                .map(c -> ConnectorResponse.builder()
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
                        .build())
                .toList();

        long availableCount = connectorResponses.stream().filter(c -> c.getStatus() == ConnectorStatus.AVAILABLE).count();

        return StationAvailabilityResponse.builder()
                .stationId(station.getId())
                .totalConnectors(connectorResponses.size())
                .availableConnectors((int) availableCount)
                .connectors(connectorResponses)
                .build();
    }
}
