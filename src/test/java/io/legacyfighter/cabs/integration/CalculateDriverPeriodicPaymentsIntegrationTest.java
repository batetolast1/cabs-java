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
import java.util.Map;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CalculateDriverPeriodicPaymentsIntegrationTest {

    @Autowired
    DriverFeeService driverFeeService;

    @Autowired
    TransitRepository transitRepository;

    @Autowired
    DriverFeeRepository driverFeeRepository;

    @Autowired
    DriverService driverService;

    @Test
    void calculateMonthlyPayment() {
        // given
        Driver driver = aDriver();
        //and
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 5, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 10, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 15, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 25, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 30, 14, 0));
        // and
        driverHasFee(driver, DriverFee.FeeType.FLAT, 100, 0);

        // when
        Integer integer = driverService.calculateDriverMonthlyPayment(driver.getId(), 2022, 4);

        // then
        assertThat(integer).isEqualTo(11600);
    }

    @Test
    void calculateYearlyPayment() {
        // given
        Driver driver = aDriver();
        //and
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 5, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 10, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 15, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 25, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 30, 14, 0));
        // and
        aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 5, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 15, 14, 0));
        aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 25, 14, 0));

        // and
        driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 50, 10);

        // when
        Map<Month, Integer> payments = driverService.calculateDriverYearlyPayment(driver.getId(), 2022);

        // then
        assertEquals(0, payments.get(Month.JANUARY));
        assertEquals(0, payments.get(Month.FEBRUARY));
        assertEquals(0, payments.get(Month.MARCH));
        assertEquals(6100, payments.get(Month.APRIL));
        assertEquals(0, payments.get(Month.MAY));
        assertEquals(0, payments.get(Month.JUNE));
        assertEquals(0, payments.get(Month.JULY));
        assertEquals(0, payments.get(Month.AUGUST));
        assertEquals(0, payments.get(Month.SEPTEMBER));
        assertEquals(3050, payments.get(Month.OCTOBER));
        assertEquals(0, payments.get(Month.NOVEMBER));
        assertEquals(0, payments.get(Month.DECEMBER));
    }

    private Driver aDriver() {
        return driverService.createDriver("9AAAA123456AA1AA", "last name", "first name", REGULAR, ACTIVE, "photo");
    }

    private void aTransit(Driver driver, LocalDateTime dateTime) {
        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setDriver(driver);
        transit.setStatus(Transit.Status.DRAFT);
        transit.setKm(10.0f);
        transit.setStatus(Transit.Status.COMPLETED);
        transit.setPrice(transit.calculateFinalCosts());

        transitRepository.save(transit);
    }

    private void driverHasFee(Driver driver, DriverFee.FeeType feeType, int amount, int minimumFee) {
        DriverFee driverFee = new DriverFee();
        driverFee.setDriver(driver);
        driverFee.setFeeType(feeType);
        driverFee.setAmount(amount);
        driverFee.setMin(minimumFee);

        driverFeeRepository.save(driverFee);
    }
}