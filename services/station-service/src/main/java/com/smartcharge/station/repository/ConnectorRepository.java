package com.smartcharge.station.repository;

import com.smartcharge.station.entity.Connector;
import com.smartcharge.station.entity.ConnectorStatus;
import com.smartcharge.station.entity.ConnectorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {

    List<Connector> findByStationId(UUID stationId);

    List<Connector> findByStationIdAndStatus(UUID stationId, ConnectorStatus status);

    List<Connector> findByConnectorType(ConnectorType connectorType);

    Optional<Connector> findByConnectorCode(String connectorCode);
}
