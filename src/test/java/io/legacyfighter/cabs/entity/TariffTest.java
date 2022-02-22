package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.Dates;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffTest {

    @Test
    void newYearsEveTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.NEW_YEARS_EVE);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(4600));
        assertThat(tariff.getKmRate()).isEqualTo(3.50f);
        assertThat(tariff.getName()).isEqualTo("Sylwester");
    }

    @Test
    void newYearMorningTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.NEW_YEAR_MORNING);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(4600));
        assertThat(tariff.getKmRate()).isEqualTo(3.50f);
        assertThat(tariff.getName()).isEqualTo("Sylwester");
    }

    @Test
    void fridayEveningTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.FRIDAY_EVENING);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(3500));
        assertThat(tariff.getKmRate()).isEqualTo(2.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend+");
    }

    @Test
    void SaturdayMorningTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.SATURDAY_MORNING);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(3500));
        assertThat(tariff.getKmRate()).isEqualTo(2.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend+");
    }

    @Test
    void saturdayEveningTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.SATURDAY_EVENING);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(3500));
        assertThat(tariff.getKmRate()).isEqualTo(2.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend+");
    }

    @Test
    void sundayMorningTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.SUNDAY_MORNING);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(3500));
        assertThat(tariff.getKmRate()).isEqualTo(2.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend+");
    }

    @Test
    void saturdayTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.SATURDAY);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(2300));
        assertThat(tariff.getKmRate()).isEqualTo(1.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend");
    }

    @Test
    void sundayTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.SUNDAY);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(2300));
        assertThat(tariff.getKmRate()).isEqualTo(1.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend");
    }

    @Test
    void workingDayTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.WORKING_DAY);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(1900));
        assertThat(tariff.getKmRate()).isEqualTo(1.00f);
        assertThat(tariff.getName()).isEqualTo("Standard");
    }
}