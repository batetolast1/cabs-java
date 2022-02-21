package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.Dates;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffTest {

    @Test
    void standardTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.STANDARD_DAY);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(1900));
        assertThat(tariff.getKmRate()).isEqualTo(1.00f);
        assertThat(tariff.getName()).isEqualTo("Standard");
    }

    @Test
    void weekendTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.WEEKEND);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(2300));
        assertThat(tariff.getKmRate()).isEqualTo(1.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend");
    }

    @Test
    void weekendPlusTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.WEEKEND_PLUS);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(3500));
        assertThat(tariff.getKmRate()).isEqualTo(2.50f);
        assertThat(tariff.getName()).isEqualTo("Weekend+");
    }

    @Test
    void newYearsEveTariffShouldBeDisplayedAndCalculated() {
        // when
        Tariff tariff = Tariff.ofTime(Dates.NEW_YEARS_EVE);

        // then
        assertThat(tariff.calculateCost(Distance.ofKm(10.0f))).isEqualTo(new Money(4600));
        assertThat(tariff.getKmRate()).isEqualTo(3.50f);
        assertThat(tariff.getName()).isEqualTo("Sylwester");
    }
}