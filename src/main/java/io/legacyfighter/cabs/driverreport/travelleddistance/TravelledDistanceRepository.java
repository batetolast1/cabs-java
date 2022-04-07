package io.legacyfighter.cabs.driverreport.travelleddistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
interface TravelledDistanceRepository extends JpaRepository<TravelledDistance, UUID> {

    @Query("SELECT td " +
            "FROM TravelledDistance td " +
            "WHERE td.timeSlot.beginning <= :timestamp " +
            "AND :timestamp < td.timeSlot.end " +
            "AND td.driverId = :driverId")
    TravelledDistance findTravelledDistanceByTimestampAndDriverId(Instant timestamp, Long driverId);

    TravelledDistance findTravelledDistanceByTimeSlotAndDriverId(TimeSlot timeSlot, Long driverId);

    @Query(value = "SELECT COALESCE(SUM(_inner.km), 0) " +
            "FROM " +
            "(SELECT td.km, td.end " +
            "FROM travelled_distance td " +
            "WHERE td.beginning >= :beginning " +
            "AND td.driver_id = :driverId) AS _inner " +
            "WHERE _inner.end <= :end",
            nativeQuery = true)
    double calculateDistance(Instant beginning, Instant end, Long driverId);
}
