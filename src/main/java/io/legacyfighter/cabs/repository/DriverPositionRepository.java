package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.dto.DriverPositionDTOV2;
import io.legacyfighter.cabs.entity.DriverPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface DriverPositionRepository extends JpaRepository<DriverPosition, Long> {

    @Query(value = "" +
            "SELECT new io.legacyfighter.cabs.dto.DriverPositionDTOV2(p.driver, avg(p.latitude), avg(p.longitude), max(p.seenAt)) " +
            "FROM DriverPosition p " +
            "WHERE p.latitude BETWEEN ?1 AND ?2 AND p.longitude BETWEEN ?3 AND ?4 AND p.seenAt >= ?5 " +
            "GROUP BY p.driver.id")
    List<DriverPositionDTOV2> findAverageDriverPositionSince(double latitudeMin,
                                                             double latitudeMax,
                                                             double longitudeMin,
                                                             double longitudeMax,
                                                             Instant date);

    List<DriverPosition> findByDriverIdAndSeenAtGreaterThanEqualAndSeenAtLessThanEqualOrderBySeenAtAsc(Long driverId,
                                                                                                       Instant from,
                                                                                                       Instant to);

    List<DriverPosition> findAllBySeenAtBefore(Instant date);
}
