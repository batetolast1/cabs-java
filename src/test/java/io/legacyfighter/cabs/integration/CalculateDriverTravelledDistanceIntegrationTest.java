package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.service.DriverTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CalculateDriverTravelledDistanceIntegrationTest {

    private static final Instant NOON = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant NOON_FIVE = NOON.plus(5, ChronoUnit.MINUTES);
    private static final Instant NOON_TEN = NOON_FIVE.plus(5, ChronoUnit.MINUTES);

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private DriverTrackingService driverTrackingService;

    @Test
    void canCalculateDistanceWhenDriverNotFound() {
        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(0L, NOON, NOON_FIVE);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void distanceIsZeroWhenZeroPositions() {
        // given
        Driver driver = fixtures.aDriver();

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), NOON, NOON_FIVE);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void travelledDistanceWithoutMultiplePositionsIsZero() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON);

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), NOON, NOON_FIVE);

        // then
        assertThat(travelledDistance).isEqualTo(Distance.ZERO);
    }

    @Test
    void canCalculateTravelledDistanceFromShortTransit() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON);

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), NOON, NOON_FIVE);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("22.278km");
    }

    @Test
    void canCalculateTravelledDistanceWithBreakBetweenTransits() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON);
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON_FIVE);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON_FIVE);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON_FIVE);

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), NOON, NOON_FIVE);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("66.835km");
    }

    @Test
    void canCalculateTravelledDistanceWithMultipleBreaks() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON);
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON_FIVE);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON_FIVE);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON_FIVE);
        // and
        driverTrackingService.registerPosition(driver.getId(), 53.3, -1.72, NOON_TEN);
        driverTrackingService.registerPosition(driver.getId(), 53.4, -1.73, NOON_TEN);
        driverTrackingService.registerPosition(driver.getId(), 53.5, -1.74, NOON_TEN);

        // when
        Distance travelledDistance = driverTrackingService.calculateTravelledDistance(driver.getId(), NOON, NOON_TEN);

        // then
        assertThat(travelledDistance.printIn("km")).isEqualTo("111.392km");
    }
}