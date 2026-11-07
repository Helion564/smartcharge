package com.smartcharge.session.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "session_data_point",
    indexes = {
        @Index(name = "idx_data_point_session", columnList = "session_id"),
        @Index(name = "idx_data_point_time", columnList = "recorded_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChargingSession session;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "current_power_kw", nullable = false)
    private double currentPowerKw;

    @Column(name = "energy_kwh", nullable = false)
    private double energyKwh;

    @Column(name = "state_of_charge", nullable = false)
    private double stateOfCharge; // battery %

    @Column(name = "current_amps")
    private double currentAmps;

    @Column(name = "voltage_volts")
    private double voltageVolts;

    @Column(name = "temperature")
    private double temperature;
}

