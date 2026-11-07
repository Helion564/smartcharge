package com.smartcharge.booking.repository;

import com.smartcharge.booking.entity.Booking;
import com.smartcharge.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for {@link Booking} entities.
 * Provides standard CRUD plus domain-specific queries for overlap detection,
 * user/station views, and time-range analytics.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Retrieves all bookings for a given user, ordered by start time descending.
     */
    Page<Booking> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    /**
     * Retrieves bookings for a user filtered by status.
     */
    Page<Booking> findByUserIdAndStatusOrderByStartTimeDesc(String userId, BookingStatus status, Pageable pageable);

    /**
     * Retrieves all bookings for a given station.
     */
    Page<Booking> findByStationIdOrderByStartTimeDesc(String stationId, Pageable pageable);

    /**
     * Retrieves bookings for a station filtered by status.
     */
    Page<Booking> findByStationIdAndStatusOrderByStartTimeDesc(String stationId, BookingStatus status, Pageable pageable);

    /**
     * Retrieves all bookings for a given connector.
     */
    List<Booking> findByConnectorIdAndStatusIn(String connectorId, List<BookingStatus> statuses);

    /**
     * Finds all CONFIRMED or PENDING bookings for a connector that overlap with the given time window.
     * Overlap condition: NOT (bookingEndTime &le; requestedStartTime OR bookingStartTime &ge; requestedEndTime)
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.connectorId = :connectorId
              AND b.status IN ('CONFIRMED', 'PENDING')
              AND NOT (b.endTime <= :startTime OR b.startTime >= :endTime)
            """)
    List<Booking> findOverlappingBookings(@Param("connectorId") String connectorId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * Same as above but excludes a specific booking (used during update flow).
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.connectorId = :connectorId
              AND b.id <> :excludeId
              AND b.status IN ('CONFIRMED', 'PENDING')
              AND NOT (b.endTime <= :startTime OR b.startTime >= :endTime)
            """)
    List<Booking> findOverlappingBookingsExcluding(@Param("connectorId") String connectorId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("excludeId") UUID excludeId);

    /**
     * Finds CONFIRMED/PENDING bookings within a given time range (for energy optimization service).
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status IN ('CONFIRMED', 'PENDING')
              AND b.startTime >= :from
              AND b.endTime <= :to
            ORDER BY b.startTime ASC
            """)
    List<Booking> findBookingsInTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Finds CONFIRMED bookings that have passed their end time (candidates for EXPIRED/NO_SHOW).
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'CONFIRMED'
              AND b.endTime < :now
            """)
    List<Booking> findExpiredConfirmedBookings(@Param("now") LocalDateTime now);

    /**
     * Finds a booking by ID and userId (ownership check).
     */
    Optional<Booking> findByIdAndUserId(UUID id, String userId);

    /**
     * Counts active (CONFIRMED/PENDING) bookings for a connector in the future.
     */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.connectorId = :connectorId
              AND b.status IN ('CONFIRMED', 'PENDING')
              AND b.endTime > :now
            """)
    long countActiveBookingsForConnector(@Param("connectorId") String connectorId,
                                          @Param("now") LocalDateTime now);

    /**
     * Bulk update: marks all expired CONFIRMED bookings as EXPIRED.
     */
    @Modifying
    @Query("""
            UPDATE Booking b SET b.status = 'EXPIRED'
            WHERE b.status = 'CONFIRMED'
              AND b.endTime < :now
              AND b.actualStartTime IS NULL
            """)
    int expireStaleBookings(@Param("now") LocalDateTime now);
}
