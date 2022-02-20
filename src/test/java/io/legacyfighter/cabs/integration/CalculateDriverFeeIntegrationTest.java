package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.DriverFeeRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.service.DriverFeeService;
import io.legacyfighter.cabs.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CalculateDriverFeeIntegrationTest {

    @Autowired
    DriverFeeService driverFeeService;

    @Autowired
    DriverFeeRepository driverFeeRepository;

    @Autowired
    TransitRepository transitRepository;

    @Autowired
    DriverService driverService;

    @Test
    void shouldCalculateDriverFlatFee() {
        // given
        Driver driver = aDriver();
        // and
        Transit transit = aTransit(driver);
        // and
        driverHasFee(driver, DriverFee.FeeType.FLAT, 50, 0);

        // when
        Integer integer = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(integer).isEqualTo(1850);
    }

    @Test
    void shouldCalculateDriverPercentageFee() {
        // given
        Driver driver = aDriver();
        // and
        Transit transit = aTransit(driver);
        // and
        driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 50, 0);

        // when
        Integer integer = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(integer).isEqualTo(950);
    }

    @Test
    void shouldUseMinimumFee() {
        // given
        Driver driver = aDriver();
        // and
        Transit transit = aTransit(driver);
        // and
        int minimumFee = 100;
        // and
        driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 0, minimumFee);

        // when
        Integer fee = driverFeeService.calculateDriverFee(transit.getId());

        // then
        assertThat(fee).isEqualTo(minimumFee);

    }

    private void driverHasFee(Driver driver, DriverFee.FeeType feeType, int amount, int minimumFee) {
        DriverFee driverFee = new DriverFee();
        driverFee.setDriver(driver);
        driverFee.setFeeType(feeType);
        driverFee.setAmount(amount);
        driverFee.setMin(minimumFee);

        driverFeeRepository.save(driverFee);
    }

    private Driver aDriver() {
        return driverService.createDriver("9AAAA123456AA1AA", "last name", "first name", REGULAR, ACTIVE, "photo");
    }

    private Transit aTransit(Driver driver) {
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.APRIL, 20, 14, 0);

        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setDriver(driver);
        transit.setStatus(Transit.Status.DRAFT);
        transit.setKm(10.0f);
        transit.setStatus(Transit.Status.COMPLETED);
        transit.setPrice(transit.calculateFinalCosts());

        return transitRepository.save(transit);
    }
}