package com.smartcharge.station.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing a physical EV charging station.
 * Stations can originate from Open Charge Map (OCM), BEE dataset, or manual entry.
 * Connectors are managed as a child collection via cascade.
 */
@Entity
@Table(
    name = "charging_station",
    indexes = {
        @Index(name = "idx_station_status", columnList = "status"),
        @Index(name = "idx_station_operator", columnList = "operator_id"),
        @Index(name = "idx_station_ocm_id", columnList = "ocm_id"),
        @Index(name = "idx_station_lat_lng", columnList = "latitude, longitude"),
        @Index(name = "idx_station_city", columnList = "city")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * External ID from Open Charge Map; nullable for manually-entered or BEE stations.
     */
    @Column(name = "ocm_id", unique = true, nullable = true, length = 50)
    private String ocmId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address_line1", nullable = false, length = 500)
    private String addressLine1;

    @Column(name = "address_line2", length = 500)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    /**
     * UUID (as String) of the operator in the User Service.
     * Stored as string to avoid cross-service FK constraint.
     */
    @Column(name = "operator_id", nullable = false, length = 50)
    private String operatorId;

    @Column(name = "operator_name", nullable = false, length = 255)
    private String operatorName;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StationStatus status;

    /**
     * JSON array string of amenity tags, e.g. ["WIFI","RESTROOM","PARKING","CAFE"].
     * Stored as text; parsed in application layer.
     */
    @Column(name = "amenities", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String amenities;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(name = "is_24_hours", nullable = false)
    @Builder.Default
    private boolean is24Hours = false;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "website", length = 500)
    private String website;

    /**
     * Origin of this record: OCM | BEE | MANUAL
     */
    @Column(name = "data_source", nullable = false, length = 20)
    @Builder.Default
    private String dataSource = "MANUAL";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "station",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Connector> connectors = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Lifecycle callbacks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = StationStatus.ACTIVE;
        }
        if (dataSource == null) {
            dataSource = "MANUAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Bidirectional helper: adds a connector and sets its back-reference.
     */
    public void addConnector(Connector connector) {
        connectors.add(connector);
        connector.setStation(this);
    }

    /**
     * Bidirectional helper: removes a connector and clears its back-reference.
     */
    public void removeConnector(Connector connector) {
        connectors.remove(connector);
        connector.setStation(null);
    }

    /**
     * Returns count of connectors with AVAILABLE status.
     */
    public long availableConnectorCount() {
        return connectors.stream()
            .filter(c -> ConnectorStatus.AVAILABLE == c.getStatus())
            .count();
    }
}
