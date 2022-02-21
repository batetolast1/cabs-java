package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.Dates;
import io.legacyfighter.cabs.money.Money;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
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
    void estimateFinalCostBefore2019() {
        // given
        Transit transit = aDraftTransitAt(Dates.BEFORE_2019);

        // when
        Money estimateCost = transit.estimateCost();

        // then
        assertThat(estimateCost).isEqualTo(new Money(900));
    }

    @Test
    void estimateFinalCostOnNewYearsEve() {
        // given
        Transit transit = aDraftTransitAt(Dates.NEW_YEARS_EVE);

        // when
        Money estimateCost = transit.estimateCost();

        // then
        assertThat(estimateCost).isEqualTo(new Money(1100));
    }

    @Test
    void estimateFinalCostOnWeekend() {
        // given
        Transit transit = aDraftTransitAt(Dates.WEEKEND_PLUS);

        // when
        Money estimateCost = transit.estimateCost();

        // then
        assertThat(estimateCost).isEqualTo(new Money(1000));
    }

    @Test
    void estimateFinalCostOnRestOfWeekend() {
        // given
        Transit transit = aDraftTransitAt(Dates.WEEKEND);

        // when
        Money estimateCost = transit.estimateCost();

        // then
        assertThat(estimateCost).isEqualTo(new Money(800));
    }

    @Test
    void estimateFinalCostOnRegularDay() {
        // given
        Transit transit = aDraftTransitAt(Dates.REGULAR_DAY);

        // when
        Money estimateCost = transit.estimateCost();

        // then
        assertThat(estimateCost).isEqualTo(new Money(900));
    }

    @Test
    void calculateFinalCostsBefore2019() {
        // given
        Transit transit = aCompletedTransitAt(Dates.BEFORE_2019);

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(900));
    }

    @Test
    void calculateFinalCostsOnNewYearsEve() {
        // given
        Transit transit = aCompletedTransitAt(Dates.NEW_YEARS_EVE);

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(1100));
    }

    @Test
    void calculateFinalCostsOnWeekend() {
        // given
        Transit transit = aCompletedTransitAt(Dates.WEEKEND_PLUS);

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(1000));
    }

    @Test
    void calculateFinalCostsOnRestOfWeekend() {
        // given
        Transit transit = aCompletedTransitAt(Dates.WEEKEND);

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(800));
    }

    @Test
    void calculateFinalCostsOnRegularDay() {
        // given
        Transit transit = aCompletedTransitAt(Dates.REGULAR_DAY);

        // when
        Money finalCosts = transit.calculateFinalCosts();

        // then
        assertThat(finalCosts).isEqualTo(new Money(900));
    }

    private Transit aCompletedTransitAt(LocalDateTime dateTime) {
        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setStatus(Transit.Status.COMPLETED);
        return transit;
    }

    private Transit aDraftTransitAt(LocalDateTime dateTime) {
        Transit transit = new Transit();
        transit.setDateTime(dateTime.toInstant(ZoneOffset.UTC));
        transit.setStatus(Transit.Status.DRAFT);
        return transit;
    }
}