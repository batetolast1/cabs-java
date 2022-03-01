package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CalculateDriverPeriodicPaymentsIntegrationTest {

    @Autowired
    DriverService driverService;

    @Autowired
    Fixtures fixtures;

    @Test
    void calculateMonthlyPayment() {
        // given
        Driver driver = fixtures.aDriver();
        //and
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 5, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 10, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 15, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 25, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 30, 14, 0));
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 100, new Money(0));

        // when
        Money monthlyPayment = driverService.calculateDriverMonthlyPayment(driver.getId(), 2022, 4);

        // then
        assertThat(monthlyPayment).isEqualTo(new Money(11600));
    }

    @Test
    void calculateYearlyPayment() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 5, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 10, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 15, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 25, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.APRIL, 30, 14, 0));
        // and
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 5, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 15, 14, 0));
        fixtures.aTransit(driver, LocalDateTime.of(2022, Month.OCTOBER, 25, 14, 0));
        // and
        fixtures.driverHasFee(driver, DriverFee.FeeType.PERCENTAGE, 50, new Money(10));

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
}