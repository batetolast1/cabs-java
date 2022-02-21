package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.money.Money;
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
import java.util.HashMap;
import java.util.Map;

import static io.legacyfighter.cabs.entity.Driver.Status.ACTIVE;
import static io.legacyfighter.cabs.entity.Driver.Type.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;

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
        Money monthlyPayment = driverService.calculateDriverMonthlyPayment(driver.getId(), 2022, 4);

        // then
        assertThat(monthlyPayment).isEqualTo(new Money(11600));
    }

    @Test
    void calculateYearlyPayment() {
        // given
        Driver driver = aDriver();
        // and
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
        Map<Month, Money> yearlyPayment = driverService.calculateDriverYearlyPayment(driver.getId(), 2022);

        // then
        Map<Month, Money> expected = new HashMap<>();
        expected.put(Month.JANUARY, Money.ZERO);
        expected.put(Month.FEBRUARY, Money.ZERO);
        expected.put(Month.MARCH, Money.ZERO);
        expected.put(Month.APRIL, new Money(6100));
        expected.put(Month.MAY, Money.ZERO);
        expected.put(Month.JUNE, Money.ZERO);
        expected.put(Month.JULY, Money.ZERO);
        expected.put(Month.AUGUST, Money.ZERO);
        expected.put(Month.SEPTEMBER, Money.ZERO);
        expected.put(Month.OCTOBER, new Money(3050));
        expected.put(Month.NOVEMBER, Money.ZERO);
        expected.put(Month.DECEMBER, Money.ZERO);

        assertThat(yearlyPayment).containsExactlyEntriesOf(expected);
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
        driverFee.setMin(new Money(minimumFee));

        driverFeeRepository.save(driverFee);
    }
}