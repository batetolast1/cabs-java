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
        Transit transit = fixtures.aTransit(driver);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 50, 0);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(new Money(1850));
    }

    @Test
    void shouldCalculateDriverPercentageFee() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        Transit transit = fixtures.aTransit(driver);
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 50, 0);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(new Money(950));
    }

    @Test
    void shouldUseMinimumFee() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        Transit transit = fixtures.aTransit(driver);
        // and
        int minimumFee = 100;
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 0, minimumFee);

        // when
        Money driverFee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(driverFee).isEqualTo(new Money(minimumFee));
    }
}