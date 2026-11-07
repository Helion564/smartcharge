package com.smartcharge.booking.client;

import com.smartcharge.booking.dto.StationConnectorInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

/**
 * HTTP client for the station-service.
 * Uses Spring WebClient with timeout and fallback handling.
 * Failures are non-fatal — booking enrichment degrades gracefully.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StationServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${station.service.url:http://station-service}")
    private String stationServiceUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    /**
     * Fetches combined station + connector metadata for response enrichment.
     *
     * @param stationId   the station UUID as string
     * @param connectorId the connector UUID as string
     * @return Optional containing the info, or empty on failure
     */
    public Optional<StationConnectorInfo> getConnectorInfo(String stationId, String connectorId) {
        try {
            StationConnectorInfo info = webClientBuilder
                    .baseUrl(stationServiceUrl)
                    .build()
                    .get()
                    .uri("/api/v1/stations/{stationId}/connectors/{connectorId}", stationId, connectorId)
                    .retrieve()
                    .bodyToMono(StationConnectorInfo.class)
                    .timeout(TIMEOUT)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.warn("Station service returned error {}: {}", ex.getStatusCode(), ex.getMessage());
                        return Mono.empty();
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.warn("Station service call failed: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception ex) {
            log.error("Unexpected error calling station-service for stationId={}, connectorId={}: {}",
                    stationId, connectorId, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates that a connector exists and is available for booking.
     *
     * @param connectorId the connector UUID as string
     * @return true if the connector is AVAILABLE
     */
    public boolean isConnectorAvailable(String stationId, String connectorId) {
        return getConnectorInfo(stationId, connectorId)
                .map(info -> "AVAILABLE".equalsIgnoreCase(info.getStatus()))
                .orElse(true); // fail-open: if station service is down, allow booking
    }
}
