package io.legacyfighter.cabs.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
}