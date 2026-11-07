package com.smartcharge.session.service;

import com.smartcharge.session.dto.*;
import com.smartcharge.session.entity.ChargingSession;
import com.smartcharge.session.entity.SessionDataPoint;
import com.smartcharge.session.enums.SessionStatus;
import com.smartcharge.session.repository.ChargingSessionRepository;
import com.smartcharge.session.repository.SessionDataPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingSessionService {

    private final ChargingSessionRepository sessionRepository;
    private final SessionDataPointRepository dataPointRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ACTIVE_SESSION_PREFIX = "session:active:";
    private static final String SESSION_TOPIC = "session-events";

    @Transactional
    public SessionResponse startSession(String userId, StartSessionRequest request) {
        Optional<ChargingSession> existing = sessionRepository
                .findFirstByConnectorIdAndStatus(request.getConnectorId(), SessionStatus.IN_PROGRESS);
        if (existing.isPresent()) {
            throw new IllegalStateException("Connector is already in use with an active session.");
        }

        ChargingSession session = ChargingSession.builder()
                .userId(userId)
                .stationId(request.getStationId())
                .connectorId(request.getConnectorId())
                .bookingId(request.getBookingId())
                .startTime(LocalDateTime.now())
                .status(SessionStatus.IN_PROGRESS)
                .pricePerKwh(request.getPricePerKwh() != null ? request.getPricePerKwh() : 15.0)
                .vehicleModel(request.getVehicleModel())
                .vehicleRegistration(request.getVehicleRegistration())
                .build();

        ChargingSession saved = sessionRepository.save(session);

        // Cache active session in Redis
        cacheActiveSession(saved);

        // Publish SESSION_STARTED Kafka event
        publishSessionEvent("SESSION_STARTED", saved);

        return mapToResponse(saved);
    }

    @Transactional
    public void addDataPoint(UUID sessionId, SessionDataPointRequest request) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            return;
        }

        SessionDataPoint point = SessionDataPoint.builder()
                .session(session)
                .recordedAt(LocalDateTime.now())
                .currentPowerKw(request.getCurrentPowerKw())
                .energyKwh(request.getEnergyKwh())
                .stateOfCharge(request.getStateOfCharge())
                .currentAmps(request.getCurrentAmps())
                .voltageVolts(request.getVoltageVolts())
                .temperature(request.getTemperature())
                .build();

        dataPointRepository.save(point);

        session.setEnergyConsumedKwh(request.getEnergyKwh());
        session.setPowerKw(request.getCurrentPowerKw());
        session.setTotalCost(request.getEnergyKwh() * session.getPricePerKwh());
        sessionRepository.save(session);

        cacheActiveSession(session);
    }

    @Transactional
    public SessionResponse stopSession(UUID sessionId, String userId, StopSessionRequest request, boolean isAdmin) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!isAdmin && !session.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to stop this session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            return mapToResponse(session);
        }

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        session.setStatus(SessionStatus.COMPLETED);
        session.setStopReason(request != null && request.getStopReason() != null ? request.getStopReason() : "MANUAL");

        if (request != null && request.getFinalEnergyKwh() != null) {
            session.setEndEnergyKwh(request.getFinalEnergyKwh());
            session.setEnergyConsumedKwh(request.getFinalEnergyKwh());
            session.setTotalCost(request.getFinalEnergyKwh() * session.getPricePerKwh());
        }

        ChargingSession saved = sessionRepository.save(session);

        // Remove from Redis active sessions
        try {
            redisTemplate.delete(ACTIVE_SESSION_PREFIX + session.getConnectorId());
        } catch (Exception e) {
            log.warn("Redis delete error: {}", e.getMessage());
        }

        // Publish SESSION_COMPLETED event
        publishSessionEvent("SESSION_COMPLETED", saved);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ActiveSessionResponse getActiveSession(String connectorId) {
        ChargingSession session = sessionRepository
                .findFirstByConnectorIdAndStatus(connectorId, SessionStatus.IN_PROGRESS)
                .orElseThrow(() -> new IllegalArgumentException("No active session on connector: " + connectorId));

        long duration = Duration.between(session.getStartTime(), LocalDateTime.now()).toMinutes();

        return ActiveSessionResponse.builder()
                .sessionId(session.getId())
                .userId(session.getUserId())
                .stationId(session.getStationId())
                .connectorId(session.getConnectorId())
                .startTime(session.getStartTime())
                .currentEnergyKwh(session.getEnergyConsumedKwh())
                .currentPowerKw(session.getPowerKw())
                .currentCost(session.getTotalCost())
                .durationMinutes(duration)
                .build();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(UUID sessionId, String userId, boolean isAdmin) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!isAdmin && !session.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to view this session");
        }

        return mapToResponse(session);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(String userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByStartTimeDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> getStationSessions(String stationId, Pageable pageable) {
        return sessionRepository.findByStationIdOrderByStartTimeDesc(stationId, pageable)
                .map(this::mapToResponse);
    }

    private void cacheActiveSession(ChargingSession session) {
        try {
            redisTemplate.opsForValue().set(
                    ACTIVE_SESSION_PREFIX + session.getConnectorId(),
                    session.getId().toString(),
                    Duration.ofHours(24)
            );
        } catch (Exception e) {
            log.warn("Failed to cache active session in Redis: {}", e.getMessage());
        }
    }

    private void publishSessionEvent(String eventType, ChargingSession session) {
        try {
            SessionEvent event = SessionEvent.builder()
                    .eventType(eventType)
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .stationId(session.getStationId())
                    .connectorId(session.getConnectorId())
                    .bookingId(session.getBookingId())
                    .energyConsumedKwh(session.getEnergyConsumedKwh())
                    .totalCost(session.getTotalCost())
                    .timestamp(Instant.now())
                    .build();
            kafkaTemplate.send(SESSION_TOPIC, session.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish session Kafka event: {}", e.getMessage());
        }
    }

    private SessionResponse mapToResponse(ChargingSession session) {
        Long duration = null;
        if (session.getStartTime() != null) {
            LocalDateTime end = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
            duration = Duration.between(session.getStartTime(), end).toMinutes();
        }

        List<SessionResponse.SessionDataPointResponse> points = dataPointRepository
                .findBySessionIdOrderByRecordedAtDesc(session.getId(), PageRequest.of(0, 10))
                .stream()
                .map(p -> SessionResponse.SessionDataPointResponse.builder()
                        .recordedAt(p.getRecordedAt())
                        .currentPowerKw(p.getCurrentPowerKw())
                        .energyKwh(p.getEnergyKwh())
                        .stateOfCharge(p.getStateOfCharge())
                        .currentAmps(p.getCurrentAmps())
                        .voltageVolts(p.getVoltageVolts())
                        .temperature(p.getTemperature())
                        .build())
                .collect(Collectors.toList());

        return SessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .stationId(session.getStationId())
                .connectorId(session.getConnectorId())
                .bookingId(session.getBookingId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .startEnergyKwh(session.getStartEnergyKwh())
                .endEnergyKwh(session.getEndEnergyKwh())
                .energyConsumedKwh(session.getEnergyConsumedKwh())
                .powerKw(session.getPowerKw())
                .status(session.getStatus())
                .totalCost(session.getTotalCost())
                .pricePerKwh(session.getPricePerKwh())
                .vehicleModel(session.getVehicleModel())
                .vehicleRegistration(session.getVehicleRegistration())
                .stopReason(session.getStopReason())
                .durationMinutes(duration)
                .recentDataPoints(points)
                .build();
    }
}
