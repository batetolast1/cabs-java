package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TravelledDistanceTest {

    @Test
    void cannotCreateTravelledDistanceWithNull() {
        // given
        Driver driver = Driver.withId(0L);
        Long driverId = 1L;
        Instant now = Instant.now();
        TimeSlot timeSlot = TimeSlot.of(now, now.plusSeconds(10));
        DriverPosition driverPosition = new DriverPosition(driver, now, 1.0, 1.1);

        // when
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(null, timeSlot, driverPosition));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, null, driverPosition));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, timeSlot, null));
    }

    @Test
    void cannotCreateTravelledDistanceForDifferentDriver() {
        // given
        Driver driver = Driver.withId(0L);
        Long driverId = 1L;
        Instant now = Instant.now();
        TimeSlot timeSlot = TimeSlot.of(now, now.plusSeconds(10));
        DriverPosition driverPosition = new DriverPosition(driver, now, 1.0, 1.1);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new TravelledDistance(driverId, timeSlot, driverPosition));
    }

    @Test
    void canCreateTravelledDistance() {
        // given
        Driver driver = Driver.withId(1L);
        Long driverId = 1L;
        Instant now = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).atZone(ZonedDateTime.now().getZone()).toInstant();
        TimeSlot timeSlot = TimeSlot.of(now, now.plusSeconds(2));
        DriverPosition driverPosition = new DriverPosition(driver, now, 1.0, 1.1);

        // when
        TravelledDistance travelledDistance = new TravelledDistance(driverId, timeSlot, driverPosition);

        // then
        assertThat(travelledDistance.getLastLatitude()).isEqualTo(1.0);
        assertThat(travelledDistance.getLastLongitude()).isEqualTo(1.1);
        assertThat(travelledDistance.getDistance()).isEqualTo(Distance.ZERO);

        assertThat(travelledDistance.contains(now.minusSeconds(1))).isFalse();
        assertThat(travelledDistance.contains(now)).isTrue();
        assertThat(travelledDistance.contains(now.plusSeconds(1))).isTrue();
        assertThat(travelledDistance.contains(now.plusSeconds(2))).isFalse();
        assertThat(travelledDistance.contains(now.plusSeconds(3))).isFalse();

        assertThat(travelledDistance.endsAt(now.plusSeconds(1))).isFalse();
        assertThat(travelledDistance.endsAt(now.plusSeconds(2))).isTrue();
        assertThat(travelledDistance.endsAt(now.plusSeconds(3))).isFalse();

        assertThat(travelledDistance.isBefore(now.plusSeconds(1))).isFalse();
        assertThat(travelledDistance.isBefore(now.plusSeconds(2))).isFalse();
        assertThat(travelledDistance.isBefore(now.plusSeconds(3))).isTrue();

    }

    @Test
    void canAddDistanceToTravelledDistance() {
        // given
        TravelledDistance travelledDistance = aTravelledDistance();

        // when
        travelledDistance.addDistance(Distance.ofKm(20), 2.0, 2.1);
        travelledDistance.addDistance(Distance.ofKm(30), 3.0, 3.1);

        // then
        assertThat(travelledDistance.getLastLatitude()).isEqualTo(3.0);
        assertThat(travelledDistance.getLastLongitude()).isEqualTo(3.1);
        assertThat(travelledDistance.getDistance()).isEqualTo(Distance.ofKm(50));
    }

    private TravelledDistance aTravelledDistance() {
        Driver driver = Driver.withId(1L);
        Long driverId = 1L;
        Instant now = Instant.now();
        TimeSlot timeSlot = TimeSlot.of(now, now.plusSeconds(10));
        DriverPosition driverPosition = new DriverPosition(driver, now, 1.0, 1.1);
        return new TravelledDistance(driverId, timeSlot, driverPosition);
    }
}
