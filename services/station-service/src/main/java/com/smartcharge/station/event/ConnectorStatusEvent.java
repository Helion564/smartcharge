package com.smartcharge.station.event;

import com.smartcharge.station.entity.ConnectorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorStatusEvent {
    private UUID stationId;
    private UUID connectorId;
    private String connectorCode;
    private ConnectorStatus oldStatus;
    private ConnectorStatus newStatus;
    private Instant timestamp;
}

