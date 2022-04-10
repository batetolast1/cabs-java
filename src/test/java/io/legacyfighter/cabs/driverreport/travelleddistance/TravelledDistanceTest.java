package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TravelledDistanceTest {

    @Test
    void cannotCreateTravelledDistanceWithNullDriverId() {
        // given
        Instant now = Instant.now();
        TimeSlot timeSlot = TimeSlot.timeSlotThatContains(now);
        double latitude = 1.0;
        double longitude = 1.1;

        // when
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(null, timeSlot, latitude, longitude, now));
    }

    @Test
    void cannotCreateTravelledDistanceWithNullTimeSlot() {
        // given
        Instant now = Instant.now();
        Long driverId = 1L;
        double latitude = 1.0;
        double longitude = 1.1;

        // when
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, null, latitude, longitude, now));
    }

    @Test
    void cannotCreateTravelledDistanceWithNullLastPositionTime() {
        // given
        Instant now = Instant.now();
        TimeSlot timeSlot = TimeSlot.timeSlotThatContains(now);
        Long driverId = 1L;
        double latitude = 1.0;
        double longitude = 1.1;

        // when
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, timeSlot, latitude, longitude, null));
    }

    @Test
    void cannotCreateTravelledDistanceWithTimeSlotNotContainingLastPositionTime() {
        // given
        Instant now = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).atZone(ZonedDateTime.now().getZone()).toInstant();
        TimeSlot timeSlot = TimeSlot.timeSlotThatContains(now);
        Long driverId = 1L;
        double latitude = 1.0;
        double longitude = 1.1;
        Instant invalidLastPositionTime = now.minus(1, ChronoUnit.NANOS);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, timeSlot, latitude, longitude, invalidLastPositionTime));
    }

    @Test
    void canCreateTravelledDistance() {
        // given
        Long driverId = 1L;
        Instant now = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).atZone(ZonedDateTime.now().getZone()).toInstant();
        TimeSlot timeSlot = TimeSlot.timeSlotThatContains(now);
        double latitude = 1.0;
        double longitude = 1.1;

        // when
        TravelledDistance travelledDistance = new TravelledDistance(driverId, timeSlot, latitude, longitude, now);

        // then
        assertThat(travelledDistance.getLastLatitude()).isEqualTo(1.0);
        assertThat(travelledDistance.getLastLongitude()).isEqualTo(1.1);
        assertThat(travelledDistance.getDistance()).isEqualTo(Distance.ZERO);
        assertThat(travelledDistance.getLastPositionTime()).isEqualTo(now);

        assertThat(travelledDistance.contains(now.minusNanos(1))).isFalse();
        assertThat(travelledDistance.contains(now)).isTrue();
        assertThat(travelledDistance.contains(now.plus(5, ChronoUnit.MINUTES))).isFalse();
        assertThat(travelledDistance.contains(now.plus(5, ChronoUnit.MINUTES).plusNanos(1))).isFalse();

        assertThat(travelledDistance.endsAt(now.plus(5, ChronoUnit.MINUTES).minusNanos(1))).isFalse();
        assertThat(travelledDistance.endsAt(now.plus(5, ChronoUnit.MINUTES))).isTrue();
        assertThat(travelledDistance.endsAt(now.plus(5, ChronoUnit.MINUTES).plusNanos(1))).isFalse();

        assertThat(travelledDistance.isBefore(now.plus(5, ChronoUnit.MINUTES).minusNanos(1))).isFalse();
        assertThat(travelledDistance.isBefore(now.plus(5, ChronoUnit.MINUTES))).isFalse();
        assertThat(travelledDistance.isBefore(now.plus(5, ChronoUnit.MINUTES).plusNanos(1))).isTrue();
    }

    @Test
    void canAddDistanceToTravelledDistance() {
        // given
        Instant now = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).atZone(ZonedDateTime.now().getZone()).toInstant();
        // and
        TravelledDistance travelledDistance = aTravelledDistance(now);

        // when
        travelledDistance.addDistance(Distance.ofKm(20), 2.0, 2.1, now.plusNanos(1));
        travelledDistance.addDistance(Distance.ofKm(30), 3.0, 3.1, now.plusNanos(2));

        // then
        assertThat(travelledDistance.getLastLatitude()).isEqualTo(3.0);
        assertThat(travelledDistance.getLastLongitude()).isEqualTo(3.1);
        assertThat(travelledDistance.getDistance()).isEqualTo(Distance.ofKm(50));
    }

    @Test
    void cannotAddTravelledDistanceWhenLastPositionDateIsInvalid() {
        // given
        Instant now = LocalDateTime.of(2022, Month.APRIL, 3, 12, 1).atZone(ZonedDateTime.now().getZone()).toInstant();
        Instant beforeNow = now.minusNanos(1);
        // and
        TravelledDistance travelledDistance = aTravelledDistance(now);
        // and
        Distance distance = Distance.ofKm(20);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> travelledDistance.addDistance(distance, 2.0, 3.0, now));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> travelledDistance.addDistance(distance, 2.0, 3.0, beforeNow));
    }

    private TravelledDistance aTravelledDistance(Instant lastPositionDate) {
        Long driverId = 1L;
        TimeSlot timeSlot = TimeSlot.timeSlotThatContains(lastPositionDate);
        double latitude = 1.0;
        double longitude = 1.1;
        return new TravelledDistance(driverId, timeSlot, latitude, longitude, lastPositionDate);
    }
}
