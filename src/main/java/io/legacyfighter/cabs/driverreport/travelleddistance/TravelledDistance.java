package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.DriverPosition;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class TravelledDistance {

    @Id
    private UUID intervalId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private double lastLatitude;

    @Column(nullable = false)
    private double lastLongitude;

    @Embedded
    private Distance distance;

    protected TravelledDistance() {
    }

    TravelledDistance(Long driverId,
                      TimeSlot timeSlot,
                      DriverPosition driverPosition) {
        Objects.requireNonNull(driverId);
        Objects.requireNonNull(timeSlot);
        Objects.requireNonNull(driverPosition);

        if (!Objects.equals(driverId, driverPosition.getDriver().getId())) {
            throw new IllegalArgumentException();
        }

        this.intervalId = UUID.randomUUID();
        this.driverId = driverId;
        this.timeSlot = timeSlot;
        this.lastLatitude = driverPosition.getLatitude();
        this.lastLongitude = driverPosition.getLongitude();
        this.distance = Distance.ZERO;
    }

    double getLastLatitude() {
        return this.lastLatitude;
    }

    double getLastLongitude() {
        return this.lastLongitude;
    }

    Distance getDistance() {
        return Distance.ofKm(this.distance.toKmInDouble());
    }

    void addDistance(Distance travelled, double latitude, double longitude) {
        this.distance = distance.add(travelled);
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
    }

    boolean contains(Instant timestamp) {
        return this.timeSlot.contains(timestamp);
    }

    boolean endsAt(Instant timestamp) {
        return this.timeSlot.endsAt(timestamp);
    }

    boolean isBefore(Instant timestamp) {
        return this.timeSlot.isBefore(timestamp);
    }
}
