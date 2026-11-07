package com.smartcharge.session.entity;

import com.smartcharge.session.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "charging_session",
    indexes = {
        @Index(name = "idx_session_user", columnList = "user_id"),
        @Index(name = "idx_session_station", columnList = "station_id"),
        @Index(name = "idx_session_connector", columnList = "connector_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_start_time", columnList = "start_time")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "station_id", nullable = false)
    private String stationId;

    @Column(name = "connector_id", nullable = false)
    private String connectorId;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_energy_kwh", nullable = false)
    @Builder.Default
    private double startEnergyKwh = 0.0;

    @Column(name = "end_energy_kwh")
    private double endEnergyKwh;

    @Column(name = "energy_consumed_kwh", nullable = false)
    @Builder.Default
    private double energyConsumedKwh = 0.0;

    @Column(name = "power_kw", nullable = false)
    @Builder.Default
    private double powerKw = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.STARTING;

    @Column(name = "total_cost", nullable = false)
    @Builder.Default
    private double totalCost = 0.0;

    @Column(name = "price_per_kwh", nullable = false)
    @Builder.Default
    private double pricePerKwh = 15.0;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    @Column(name = "stop_reason")
    private String stopReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SessionDataPoint> dataPoints = new ArrayList<>();

    public void addDataPoint(SessionDataPoint point) {
        dataPoints.add(point);
        point.setSession(this);
    }
}

