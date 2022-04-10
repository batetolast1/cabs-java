package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;

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
    private Double lastLatitude;

    @Column(nullable = false)
    private Double lastLongitude;

    @Embedded
    private Distance distance;

    private Instant lastPositionTime;

    protected TravelledDistance() {
    }

    TravelledDistance(Long driverId,
                      TimeSlot timeSlot,
                      double lastLatitude,
                      double lastLongitude,
                      Instant lastPositionTime) {
        Objects.requireNonNull(driverId);
        Objects.requireNonNull(timeSlot);
        Objects.requireNonNull(lastPositionTime);

        if (!timeSlot.contains(lastPositionTime)) {
            throw new IllegalArgumentException();
        }

        this.intervalId = UUID.randomUUID();
        this.driverId = driverId;
        this.timeSlot = timeSlot;
        this.lastLatitude = lastLatitude;
        this.lastLongitude = lastLongitude;
        this.distance = Distance.ZERO;
        this.lastPositionTime = lastPositionTime;
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

    Instant getLastPositionTime() {
        return lastPositionTime;
    }

    void addDistance(Distance travelled,
                     double latitude,
                     double longitude,
                     Instant lastPositionTime) {
        if (!this.lastPositionTime.isBefore(lastPositionTime)) {
            throw new IllegalArgumentException();
        }

        this.distance = distance.add(travelled);
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
        this.lastPositionTime = lastPositionTime;
    }

    boolean contains(Instant timestamp) {
        return this.timeSlot.contains(timestamp);
    }

    boolean endsAt(Instant timestamp) {
        return this.timeSlot.endsAt(timestamp);
    }

    boolean isBefore(Instant timestamp) {
        return this.timeSlot.isTimeSlotBefore(timestamp);
    }
}
