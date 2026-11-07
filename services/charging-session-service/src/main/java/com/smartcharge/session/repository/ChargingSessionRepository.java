package com.smartcharge.session.repository;

import com.smartcharge.session.entity.ChargingSession;
import com.smartcharge.session.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, UUID> {

    Optional<ChargingSession> findFirstByConnectorIdAndStatus(String connectorId, SessionStatus status);

    Page<ChargingSession> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    Page<ChargingSession> findByStationIdOrderByStartTimeDesc(String stationId, Pageable pageable);

    List<ChargingSession> findByStatus(SessionStatus status);
}

