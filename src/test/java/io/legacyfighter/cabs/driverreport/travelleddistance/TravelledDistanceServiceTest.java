package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.service.DriverTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
class TravelledDistanceServiceTest {

    private static final Instant _12_00 = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant _12_05 = _12_00.plus(5, ChronoUnit.MINUTES);
    private static final Instant _12_10 = _12_00.plus(10, ChronoUnit.MINUTES);
    private static final Instant _12_15 = _12_00.plus(15, ChronoUnit.MINUTES);

    @Autowired
    private Fixtures fixtures;

    @MockBean
    private Clock clock;

    @Autowired
    private DriverTrackingService driverTrackingService;

    @Autowired
    private TravelledDistanceService travelledDistanceService;

    @Test
    void distanceIsZeroWhenZeroPositions() {
        // given
        Driver driver = fixtures.aDriver();

        // when
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void travelledDistanceWithoutMultiplePositionsIsZero() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.3, -1.72, _12_00);

        // when
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void canCalculateTravelledDistanceFromShortTransit() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));

        // when
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("22.278km");
    }

    @Test
    void canCalculateTravelledDistanceWithBreakBetweenTransits() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));
        // and
        nowIs(_12_10);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_05.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_05.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_05.plus(3, ChronoUnit.MINUTES));

        // when
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_10);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("44.557km");
    }

    @Test
    void canCalculateTravelledDistanceWithMultipleBreaks() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));
        // and
        nowIs(_12_10);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_05.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_05.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_05.plus(3, ChronoUnit.MINUTES));
        // and
        nowIs(_12_15);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_10.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_10.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_10.plus(3, ChronoUnit.MINUTES));

        // when
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_15);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("66.835km");
    }

    @Test
    void canRecalculateTravelledDistanceWhenDriverPositionsAreRegisteredNotInOrder() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));

        // then
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(travelledDistance.printIn("km")).isEqualTo("22.278km");
    }

    @Test
    void TravelledDistanceIsZeroWhenPositionDoesntChange() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, _12_00.plus(2, ChronoUnit.MINUTES));

        // then
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(travelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void travelledDistanceIsZeroWhenThereIsOnlyOnePositionAdded() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, _12_00.plus(2, ChronoUnit.MINUTES));

        // then
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(travelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void canCreateTravelledDistanceWhenSeenAtIsCurrentTimeSlot() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05.minusNanos(1));

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, _12_00.plus(2, ChronoUnit.MINUTES));

        // then
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(travelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void canCreateTravelledDistanceWhenSeenAtIsInPreviousTimeSlot() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05.plusNanos(1));

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, _12_00.plus(2, ChronoUnit.MINUTES));

        // then
        Distance travelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(travelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void distanceIsAddedToPreviousSlotWhenSeenAtIsAtTheEndOfTimeSlot() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.6, -1.75, _12_05);

        // then
        Distance firstTravelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(firstTravelledDistance.printIn("km")).isEqualTo("22.278km");

        Distance secondTravelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_05, _12_10);
        assertThat(secondTravelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void distanceIsNotAddedToPreviousSlotWhenPreviousSlotDoesntExist() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        nowIs(_12_05);

        // when
        driverTrackingService.registerPosition(driver.getId(), 53.6, -1.75, _12_05);

        // then
        Distance firstTravelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);
        assertThat(firstTravelledDistance.printIn("km")).isEqualTo("0km");

        Distance secondTravelledDistance = travelledDistanceService.calculateDistance(driver.getId(), _12_05, _12_10);
        assertThat(secondTravelledDistance.printIn("km")).isEqualTo("0km");
    }

    @Test
    void cannotAddPositionWhenSeenAtIsAfterCurrentTimeSlot() {
        // given
        Driver driver = fixtures.aDriver();
        Long driverId = driver.getId();
        // and
        nowIs(_12_00);
        Instant afterNow = _12_00.plusNanos(1);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> travelledDistanceService.addPosition(driverId, 53.4, -1.73, afterNow));
    }

    private void registerPositionForDriver(Driver driver,
                                           double latitude,
                                           double longitude,
                                           Instant seenAt) {
        driverTrackingService.registerPosition(driver.getId(), latitude, longitude, seenAt);
    }

    private void nowIs(Instant instant) {
        when(clock.instant()).thenReturn(instant);
    }
}
