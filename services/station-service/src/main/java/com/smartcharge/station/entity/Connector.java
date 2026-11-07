package com.smartcharge.station.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing an individual EV charging connector at a station.
 * Each connector can have its own type, power level, pricing, and real-time status.
 */
@Entity
@Table(
    name = "connector",
    indexes = {
        @Index(name = "idx_connector_station", columnList = "station_id"),
        @Index(name = "idx_connector_type", columnList = "connector_type"),
        @Index(name = "idx_connector_status", columnList = "status"),
        @Index(name = "idx_connector_code", columnList = "connector_code")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Parent charging station.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    /**
     * Unique human-readable code for this connector within the station,
     * e.g. "TPEZ-MUM001-C01". Used for OCPP identity and deep-linking.
     */
    @Column(name = "connector_code", unique = true, nullable = false, length = 100)
    private String connectorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 20)
    private ConnectorType connectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_level", nullable = false, length = 20)
    private PowerLevel powerLevel;

    /**
     * Actual rated power in kW (may differ from {@link PowerLevel#getPowerKw()} for
     * non-standard equipment).
     */
    @Column(name = "power_kw", nullable = false)
    private double powerKw;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ConnectorStatus status = ConnectorStatus.AVAILABLE;

    /**
     * Price in Indian Rupees per kWh.
     */
    @Column(name = "price_per_kwh", nullable = false)
    @Builder.Default
    private double pricePerKwh = 0.0;

    /**
     * Price per minute for time-based billing. 0 if kWh-only billing is used.
     */
    @Column(name = "price_per_minute", nullable = false)
    @Builder.Default
    private double pricePerMinute = 0.0;

    /**
     * Convenience flag mirroring {@code status == AVAILABLE} for quick DB queries.
     */
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean isAvailable = true;

    @Column(name = "last_status_update")
    private LocalDateTime lastStatusUpdate;

    // -------------------------------------------------------------------------
    // Lifecycle callbacks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ConnectorStatus.AVAILABLE;
        }
        isAvailable = (ConnectorStatus.AVAILABLE == status);
        lastStatusUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        isAvailable = (ConnectorStatus.AVAILABLE == status);
        lastStatusUpdate = LocalDateTime.now();
    }
}
