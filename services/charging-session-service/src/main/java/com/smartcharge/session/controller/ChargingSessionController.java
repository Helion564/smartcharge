package com.smartcharge.session.controller;

import com.smartcharge.session.dto.*;
import com.smartcharge.session.service.ChargingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class ChargingSessionController {

    private final ChargingSessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody StartSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.startSession(userId, request));
    }

    @PostMapping("/{sessionId}/data")
    public ResponseEntity<Void> addDataPoint(
            @PathVariable UUID sessionId,
            @RequestBody SessionDataPointRequest request) {
        sessionService.addDataPoint(sessionId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<SessionResponse> stopSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestBody(required = false) StopSessionRequest request) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(sessionService.stopSession(sessionId, userId, request, isAdmin));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSessionById(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(sessionService.getSessionById(sessionId, userId, isAdmin));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<SessionResponse>> getUserSessions(
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sessionService.getUserSessions(userId, pageable));
    }

    @GetMapping("/active/connector/{connectorId}")
    public ResponseEntity<ActiveSessionResponse> getActiveSession(@PathVariable String connectorId) {
        return ResponseEntity.ok(sessionService.getActiveSession(connectorId));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<Page<SessionResponse>> getStationSessions(
            @PathVariable String stationId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sessionService.getStationSessions(stationId, pageable));
    }
}
