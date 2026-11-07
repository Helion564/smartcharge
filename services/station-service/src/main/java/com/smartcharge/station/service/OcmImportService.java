package com.smartcharge.station.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcharge.station.dto.OcmImportResponse;
import com.smartcharge.station.entity.*;
import com.smartcharge.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcmImportService {

    private final StationRepository stationRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ocm.api-key:dummy}")
    private String ocmApiKey;

    @Value("${ocm.base-url:https://api.openchargemap.io/v3}")
    private String ocmBaseUrl;

    @Value("${ocm.india-country-id:101}")
    private int indiaCountryId;

    @Transactional
    public OcmImportResponse importIndiaStations(int maxResults) {
        int imported = 0;
        int skipped = 0;
        int failed = 0;

        try {
            String url = String.format("%s/poi/?output=json&countrycode=IN&countryid=%d&maxresults=%d&key=%s",
                    ocmBaseUrl, indiaCountryId, maxResults, ocmApiKey);

            String responseBody = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                return OcmImportResponse.builder()
                        .importedCount(0).skippedCount(0).failedCount(0)
                        .message("Empty response received from OCM API")
                        .build();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            if (!root.isArray()) {
                return OcmImportResponse.builder()
                        .importedCount(0).skippedCount(0).failedCount(0)
                        .message("Unexpected non-array response from OCM API")
                        .build();
            }

            for (JsonNode node : root) {
                try {
                    String ocmId = node.path("ID").asText(null);
                    if (ocmId != null && stationRepository.existsByOcmId(ocmId)) {
                        skipped++;
                        continue;
                    }

                    JsonNode addressInfo = node.path("AddressInfo");
                    String title = addressInfo.path("Title").asText("EV Charging Station");
                    String addr1 = addressInfo.path("AddressLine1").asText("Street Address");
                    String city = addressInfo.path("Town").asText("City");
                    String state = addressInfo.path("StateOrProvince").asText("State");
                    String postcode = addressInfo.path("Postcode").asText("000000");
                    double lat = addressInfo.path("Latitude").asDouble(20.5937);
                    double lng = addressInfo.path("Longitude").asDouble(78.9629);

                    JsonNode opInfo = node.path("OperatorInfo");
                    String operatorName = opInfo.path("Title").asText("Open Charge Network");

                    ChargingStation station = ChargingStation.builder()
                            .ocmId(ocmId)
                            .name(title)
                            .addressLine1(addr1)
                            .city(city)
                            .state(state)
                            .pincode(postcode)
                            .operatorId("system-ocm")
                            .operatorName(operatorName)
                            .latitude(lat)
                            .longitude(lng)
                            .status(StationStatus.ACTIVE)
                            .is24Hours(true)
                            .dataSource("OCM")
                            .build();

                    JsonNode connections = node.path("Connections");
                    if (connections.isArray() && !connections.isEmpty()) {
                        int cSeq = 1;
                        for (JsonNode cNode : connections) {
                            int connTypeId = cNode.path("ConnectionTypeID").asInt(1);
                            double powerKw = cNode.path("PowerKW").asDouble(22.0);
                            ConnectorType cType = mapOcmConnType(connTypeId);
                            PowerLevel pLevel = mapPowerLevel(powerKw);

                            Connector connector = Connector.builder()
                                    .connectorCode(String.format("OCM-%s-C%02d", ocmId != null ? ocmId : UUID.randomUUID().toString().substring(0, 5), cSeq++))
                                    .connectorType(cType)
                                    .powerLevel(pLevel)
                                    .powerKw(powerKw > 0 ? powerKw : 22.0)
                                    .status(ConnectorStatus.AVAILABLE)
                                    .pricePerKwh(12.50)
                                    .pricePerMinute(0.0)
                                    .isAvailable(true)
                                    .lastStatusUpdate(LocalDateTime.now())
                                    .build();
                            station.addConnector(connector);
                        }
                    } else {
                        // Default fallback connector
                        Connector connector = Connector.builder()
                                .connectorCode(String.format("OCM-%s-C01", ocmId != null ? ocmId : UUID.randomUUID().toString().substring(0, 5)))
                                .connectorType(ConnectorType.CCS)
                                .powerLevel(PowerLevel.DC_FAST)
                                .powerKw(50.0)
                                .status(ConnectorStatus.AVAILABLE)
                                .pricePerKwh(15.00)
                                .pricePerMinute(0.0)
                                .isAvailable(true)
                                .lastStatusUpdate(LocalDateTime.now())
                                .build();
                        station.addConnector(connector);
                    }

                    stationRepository.save(station);
                    imported++;
                } catch (Exception ex) {
                    log.error("Failed to parse individual OCM POI: {}", ex.getMessage());
                    failed++;
                }
            }

        } catch (Exception e) {
            log.error("OCM import failed: {}", e.getMessage());
            return OcmImportResponse.builder()
                    .importedCount(imported)
                    .skippedCount(skipped)
                    .failedCount(failed)
                    .message("Import partially or completely failed: " + e.getMessage())
                    .build();
        }

        return OcmImportResponse.builder()
                .importedCount(imported)
                .skippedCount(skipped)
                .failedCount(failed)
                .message("OCM import completed successfully")
                .build();
    }

    private ConnectorType mapOcmConnType(int connTypeId) {
        return switch (connTypeId) {
            case 33 -> ConnectorType.CCS;
            case 2 -> ConnectorType.CHADEMO;
            case 25 -> ConnectorType.TYPE2;
            case 1 -> ConnectorType.TYPE1;
            case 1036 -> ConnectorType.GB_T;
            case 27 -> ConnectorType.TESLA;
            default -> ConnectorType.TYPE2;
        };
    }

    private PowerLevel mapPowerLevel(double powerKw) {
        if (powerKw >= 300) return PowerLevel.HYPER_FAST;
        if (powerKw >= 100) return PowerLevel.ULTRA_FAST;
        if (powerKw >= 40) return PowerLevel.DC_FAST;
        if (powerKw >= 7) return PowerLevel.LEVEL_2;
        return PowerLevel.LEVEL_1;
    }
}

