package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.service.DriverFeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CalculateDriverFeeIntegrationTest {

    @Autowired
    DriverFeeService driverFeeService;

    @Autowired
    Fixtures fixtures;

    @Test
    void shouldCalculateDriverFlatFee() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 20, 6, 0);
        // and
        Transit transit = fixtures.aCompletedTransitAt(driver, dateTime);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 50, Money.ZERO);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(new Money(3450));
    }

    @Test
    void shouldCalculateDriverPercentageFee() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 20, 6, 0);
        // and
        Transit transit = fixtures.aCompletedTransitAt(driver, dateTime);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 50, Money.ZERO);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(new Money(1750));
    }

    @Test
    void shouldUseMinimumFee() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 20, 6, 0);
        // and
        Transit transit = fixtures.aCompletedTransitAt(driver, dateTime);
        // and
        Money minimumFee = new Money(100);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 0, minimumFee);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(minimumFee);
    }
}