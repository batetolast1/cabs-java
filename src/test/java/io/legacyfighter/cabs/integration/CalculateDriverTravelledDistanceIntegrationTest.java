package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.service.DriverTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.persistence.EntityNotFoundException;
import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
class CalculateDriverTravelledDistanceIntegrationTest {

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

    @Test
    void canCalculateDistanceWhenDriverNotFound() {
        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(0L, _12_00, _12_05);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void distanceIsZeroWhenZeroPositions() {
        // given
        Driver driver = fixtures.aDriver();

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_05);

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
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_05);

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
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_05);

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
        registerPositionForDriver(driver, 53.4, -1.73, _12_05.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_05.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_05.plus(3, ChronoUnit.MINUTES));

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_10);

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
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_15);

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
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), _12_00, _12_05);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("22.278km");
    }

    @Test
    void cannotRegisterPositionWhenDriverDoesntExist() {
        // given
        double latitude = 1.0;
        double longitude = 1.1;
        // and
        nowIs(_12_05);

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> driverTrackingService.registerPosition(0L, latitude, longitude, _12_00));
    }

    @Test
    void cannotRegisterDriverPositionWhenDriverIsNotActive() {
        // given
        Long driverId = fixtures.anInactiveDriver().getId();
        // and
        double latitude = 1.0;
        double longitude = 1.1;
        // and
        nowIs(_12_05);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> driverTrackingService.registerPosition(driverId, latitude, longitude, _12_00));
    }

    @Test
    void canRegisterDriverPosition() {
        // given
        Driver driver = fixtures.aDriver();
        Long driverId = driver.getId();
        // and
        double latitude = 1.0;
        double longitude = 1.1;
        // and
        nowIs(_12_05);

        // when
        DriverPosition driverPosition = driverTrackingService.registerPosition(driverId, latitude, longitude, _12_00);

        // then
        assertThat(driverPosition.getId()).isNotNull();
        assertThat(driverPosition.getDriver()).isEqualTo(driver);
        assertThat(driverPosition.getLatitude()).isEqualTo(latitude);
        assertThat(driverPosition.getLongitude()).isEqualTo(longitude);
        assertThat(driverPosition.getSeenAt()).isEqualTo(_12_00);
    }

    private void nowIs(Instant instant) {
        when(clock.instant()).thenReturn(instant);
    }

    private void registerPositionForDriver(Driver driver,
                                           double latitude,
                                           double longitude,
                                           Instant seenAt) {
        driverTrackingService.registerPosition(driver.getId(), latitude, longitude, seenAt);
    }
}
