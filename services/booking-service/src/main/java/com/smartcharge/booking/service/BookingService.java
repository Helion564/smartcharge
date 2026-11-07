package com.smartcharge.booking.service;

import com.smartcharge.booking.client.StationServiceClient;
import com.smartcharge.booking.dto.*;
import com.smartcharge.booking.entity.Booking;
import com.smartcharge.booking.enums.BookingStatus;
import com.smartcharge.booking.exception.BookingConflictException;
import com.smartcharge.booking.exception.BookingNotFoundException;
import com.smartcharge.booking.exception.InvalidBookingStateException;
import com.smartcharge.booking.repository.BookingRepository;
import com.smartcharge.booking.util.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StationServiceClient stationServiceClient;
    private final RedisDistributedLock redisDistributedLock;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BOOKING_TOPIC = "booking-events";

    @Transactional
    public BookingResponse createBooking(String userId, BookingRequest request) {
        if (!request.isValidTimeRange()) {
            throw new IllegalArgumentException("Invalid time range. Duration must be at least 15 minutes.");
        }

        // Try Redis lock for connector to prevent double-booking race condition
        String lockToken = redisDistributedLock.tryAcquireLock(request.getConnectorId(), 10);
        if (lockToken == null) {
            throw new BookingConflictException("Connector is currently being booked by another user. Please retry.");
        }

        try {
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    request.getConnectorId(), request.getStartTime(), request.getEndTime());

            if (!overlapping.isEmpty()) {
                throw new BookingConflictException("The selected slot overlaps with an existing booking.");
            }

            // Fetch pricing / info from station service if available
            double pricePerKwh = 15.0; // fallback
            Optional<StationConnectorInfo> infoOpt = stationServiceClient.getConnectorInfo(
                    request.getStationId(), request.getConnectorId());
            if (infoOpt.isPresent() && infoOpt.get().getPricePerKwh() > 0) {
                pricePerKwh = infoOpt.get().getPricePerKwh();
            }

            double estimatedCost = request.getEstimatedEnergyKwh() * pricePerKwh;

            Booking booking = Booking.builder()
                    .userId(userId)
                    .stationId(request.getStationId())
                    .connectorId(request.getConnectorId())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .estimatedEnergyKwh(request.getEstimatedEnergyKwh())
                    .estimatedCost(estimatedCost)
                    .pricePerKwh(pricePerKwh)
                    .status(BookingStatus.CONFIRMED)
                    .vehicleModel(request.getVehicleModel())
                    .vehicleRegistration(request.getVehicleRegistration())
                    .build();

            Booking saved = bookingRepository.save(booking);

            // Publish Kafka event
            publishBookingEvent("BOOKING_CREATED", saved, Map.of(
                    "estimatedCost", saved.getEstimatedCost(),
                    "estimatedEnergyKwh", saved.getEstimatedEnergyKwh()
            ));

            return enrichBooking(saved, infoOpt.orElse(null));
        } finally {
            redisDistributedLock.releaseLock(request.getConnectorId(), lockToken);
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new InvalidBookingStateException("You are not authorized to view this booking.");
        }

        Optional<StationConnectorInfo> info = stationServiceClient.getConnectorInfo(
                booking.getStationId(), booking.getConnectorId());

        return enrichBooking(booking, info.orElse(null));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(String userId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = (status != null)
                ? bookingRepository.findByUserIdAndStatusOrderByStartTimeDesc(userId, status, pageable)
                : bookingRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);

        return bookings.map(b -> enrichBooking(b, null));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getStationBookings(String stationId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = (status != null)
                ? bookingRepository.findByStationIdAndStatusOrderByStartTimeDesc(stationId, status, pageable)
                : bookingRepository.findByStationIdOrderByStartTimeDesc(stationId, pageable);

        return bookings.map(b -> enrichBooking(b, null));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookingsForConnector(String connectorId) {
        return bookingRepository.findByConnectorIdAndStatusIn(connectorId, List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING))
                .stream()
                .map(b -> enrichBooking(b, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, String userId, BookingCancelRequest request, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new InvalidBookingStateException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingStateException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request != null ? request.getReason() : "User cancelled");
        booking.setCancelledAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        publishBookingEvent("BOOKING_CANCELLED", saved, Map.of(
                "cancellationReason", saved.getCancellationReason() != null ? saved.getCancellationReason() : ""
        ));

        return enrichBooking(saved, null);
    }

    @Transactional
    public void completeBooking(UUID bookingId, double actualEnergyKwh, double actualCost) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setActualEnergyKwh(actualEnergyKwh);
            booking.setActualCost(actualCost);
            booking.setActualEndTime(LocalDateTime.now());
            Booking saved = bookingRepository.save(booking);

            publishBookingEvent("BOOKING_COMPLETED", saved, Map.of(
                    "actualCost", actualCost,
                    "actualEnergyKwh", actualEnergyKwh
            ));
        });
    }

    private BookingResponse enrichBooking(Booking booking, StationConnectorInfo info) {
        BookingResponse resp = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .stationId(booking.getStationId())
                .connectorId(booking.getConnectorId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .actualStartTime(booking.getActualStartTime())
                .actualEndTime(booking.getActualEndTime())
                .estimatedEnergyKwh(booking.getEstimatedEnergyKwh())
                .actualEnergyKwh(booking.getActualEnergyKwh())
                .estimatedCost(booking.getEstimatedCost())
                .actualCost(booking.getActualCost())
                .pricePerKwh(booking.getPricePerKwh())
                .status(booking.getStatus())
                .vehicleModel(booking.getVehicleModel())
                .vehicleRegistration(booking.getVehicleRegistration())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .cancelledAt(booking.getCancelledAt())
                .paymentId(booking.getPaymentId())
                .build();

        if (info != null) {
            resp.setStationName(info.getStationName());
            resp.setStationAddress(info.getStationAddress());
            resp.setConnectorType(info.getConnectorType());
            resp.setConnectorMaxPowerKw(info.getPowerKw());
        }

        return resp;
    }

    private void publishBookingEvent(String eventType, Booking booking, Map<String, Object> payload) {
        try {
            BookingEvent event = BookingEvent.of(
                    eventType,
                    booking.getId().toString(),
                    booking.getUserId(),
                    booking.getStationId(),
                    booking.getConnectorId(),
                    booking.getStatus().name(),
                    payload
            );
            kafkaTemplate.send(BOOKING_TOPIC, booking.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish booking Kafka event: {}", e.getMessage());
        }
    }
}
