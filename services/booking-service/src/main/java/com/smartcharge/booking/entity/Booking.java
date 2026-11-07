package com.smartcharge.booking.entity;

import com.smartcharge.booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a slot reservation for an EV charging connector.
 * A booking reserves a specific connector at a station for a defined time window.
 */
@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_user_id", columnList = "user_id"),
        @Index(name = "idx_booking_station_id", columnList = "station_id"),
        @Index(name = "idx_booking_connector_id", columnList = "connector_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_start_time", columnList = "start_time"),
        @Index(name = "idx_booking_connector_time", columnList = "connector_id, start_time, end_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The ID of the user who made this booking (UUID as string from X-User-Id header).
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * The ID of the charging station (UUID as string).
     */
    @Column(name = "station_id", nullable = false)
    private String stationId;

    /**
     * The ID of the specific connector at the station (UUID as string).
     */
    @Column(name = "connector_id", nullable = false)
    private String connectorId;

    /**
     * Planned start time of the charging session.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * Planned end time of the charging session.
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * Actual time the charging session started (populated after session begins).
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Actual time the charging session ended (populated after session completes).
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Estimated energy in kWh to be consumed during the session.
     */
    @Column(name = "estimated_energy_kwh", nullable = false)
    private double estimatedEnergyKwh;

    /**
     * Actual energy in kWh consumed (populated after session completes).
     */
    @Column(name = "actual_energy_kwh")
    private double actualEnergyKwh;

    /**
     * Estimated cost in INR for the session.
     */
    @Column(name = "estimated_cost", nullable = false)
    private double estimatedCost;

    /**
     * Actual cost in INR (populated after session completes).
     */
    @Column(name = "actual_cost")
    private double actualCost;

    /**
     * Current lifecycle status of this booking.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    /**
     * Vehicle model for session tracking.
     */
    @Column(name = "vehicle_model")
    private String vehicleModel;

    /**
     * Vehicle registration plate for on-site verification.
     */
    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    /**
     * Reason provided when the booking is cancelled.
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Price per kWh at the time of booking (in INR).
     */
    @Column(name = "price_per_kwh", nullable = false)
    @Builder.Default
    private double pricePerKwh = 10.0;

    /**
     * Timestamp when the booking was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the booking was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Timestamp when the booking was cancelled (null if not cancelled).
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Payment ID linking this booking to the payment service (nullable until payment is processed).
     */
    @Column(name = "payment_id")
    private String paymentId;
}
