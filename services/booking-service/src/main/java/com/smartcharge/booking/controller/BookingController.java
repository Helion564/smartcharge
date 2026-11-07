package com.smartcharge.booking.controller;

import com.smartcharge.booking.dto.BookingCancelRequest;
import com.smartcharge.booking.dto.BookingRequest;
import com.smartcharge.booking.dto.BookingResponse;
import com.smartcharge.booking.enums.BookingStatus;
import com.smartcharge.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(userId, request));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, userId, isAdmin));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, status, pageable));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestBody(required = false) BookingCancelRequest request) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, userId, request, isAdmin));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<Page<BookingResponse>> getStationBookings(
            @PathVariable String stationId,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getStationBookings(stationId, status, pageable));
    }

    @GetMapping("/connector/{connectorId}/active")
    public ResponseEntity<List<BookingResponse>> getActiveBookingsForConnector(
            @PathVariable String connectorId) {
        return ResponseEntity.ok(bookingService.getActiveBookingsForConnector(connectorId));
    }
}

