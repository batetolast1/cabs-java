package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.money.Money;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateTransitPriceTest {

    @Test
    void cannotEstimateCostWhenTransitIsCompleted() {
        // given
        Transit transit = new Transit();
        transit.setStatus(Transit.Status.COMPLETED);

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::estimateCost);
    }

    @ParameterizedTest
    @EnumSource(value = Transit.Status.class,
            names = {"CANCELLED",
                    "IN_TRANSIT",
                    "TRANSIT_TO_PASSENGER",
                    "DRAFT",
                    "DRIVER_ASSIGNMENT_FAILED",
                    "WAITING_FOR_DRIVER_ASSIGNMENT"})
    void cannotCalculateFinalCostWhenTransitIsNotCompleted(Transit.Status status) {
        // given
        Transit transit = new Transit();
        transit.setStatus(status);

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void calculateFinalCostsOnRegularDay() {
        // given
        Transit transit = aCompletedTransit(LocalDateTime.of(2022, Month.APRIL, 20, 14, 0));

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(900));
    }

    @Test
    void calculateFinalCostsOnWeekend() {
        // given
        Transit transit = aCompletedTransit(LocalDateTime.of(2022, Month.FEBRUARY, 18, 18, 0));

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(1000));
    }

    @Test
    void calculateFinalCostsOnRestOfWeekend() {
        // given
        Transit transit = aCompletedTransit(LocalDateTime.of(2022, Month.FEBRUARY, 19, 15, 0));

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(800));
    }

    @Test
    void calculateFinalCostsOnNewYear() {
        // given
        Transit transit = aCompletedTransit(LocalDateTime.of(2022, Month.JANUARY, 1, 5, 0));

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(1100));
    }

    private Transit aCompletedTransit(LocalDateTime dateTime) {
        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setStatus(Transit.Status.COMPLETED);
        return transit;
    }
}