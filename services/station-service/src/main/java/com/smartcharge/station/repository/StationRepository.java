package com.smartcharge.station.repository;

import com.smartcharge.station.entity.ChargingStation;
import com.smartcharge.station.entity.StationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<ChargingStation, UUID> {

    Optional<ChargingStation> findByOcmId(String ocmId);

    boolean existsByOcmId(String ocmId);

    Page<ChargingStation> findByStatus(StationStatus status, Pageable pageable);

    List<ChargingStation> findByOperatorId(String operatorId);

    @Query(value = "SELECT cs.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(cs.latitude)) * " +
            "cos(radians(cs.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(cs.latitude)))) AS distance " +
            "FROM charging_station cs " +
            "WHERE cs.status = 'ACTIVE' " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(cs.latitude)) * " +
            "cos(radians(cs.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(cs.latitude)))) <= :radiusKm " +
            "ORDER BY distance", nativeQuery = true)
    List<ChargingStation> findNearbyStations(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm);
}
