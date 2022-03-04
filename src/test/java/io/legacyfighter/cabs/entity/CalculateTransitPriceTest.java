package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CalculateTransitPriceTest {

    @Test
    void canEstimateCostWhenTransitIsCreated() {
        // given
        Transit transit = aCreatedTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void canEstimateCostWhenTransitIsCancelled() {
        // given
        Transit transit = aCancelledTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void canEstimateCostWhenTransitIsPublished() {
        // given
        Transit transit = aPublishedTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void canEstimateCostWhenTransitIsAccepted() {
        // given
        Transit transit = anAcceptedTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void canEstimateCostWhenTransitIsStarted() {
        // given
        Transit transit = aStartedTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void canEstimateCostWhenTransitIsFailedToAssign() {
        // given
        Transit transit = aFailedAssignmentTransit();

        // when
        Money money = transit.estimateCost();

        // then
        assertThat(money).isNotNull();
    }

    @Test
    void cannotEstimateCostWhenTransitIsCompleted() {
        // given
        Transit transit = aCompletedTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::estimateCost);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitIsCreated() {
        // given
        Transit transit = aCreatedTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitIsCancelled() {
        // given
        Transit transit = aCancelledTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitIsPublished() {
        // given
        Transit transit = aPublishedTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitFailedToAssign() {
        // given
        Transit transit = aFailedAssignmentTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitIsAccepted() {
        // given
        Transit transit = anAcceptedTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void cannotCalculateFinalCostWhenTransitIsStarted() {
        // given
        Transit transit = aStartedTransit();

        // when
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::calculateFinalCosts);
    }

    @Test
    void canCalculateFinalCostWhenTransitIsCompleted() {
        // given
        Transit transit = aCompletedTransit();

        // when
        Money money = transit.calculateFinalCosts();

        // then
        assertThat(money).isNotNull();
    }

    private Transit aCreatedTransit() {
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        return new Transit(null, null, null, null, dateTime, km);
    }

    private Transit aCancelledTransit() {
        Transit transit = aCreatedTransit();
        transit.cancel();
        return transit;
    }

    private Transit aPublishedTransit() {
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        Transit transit = new Transit(null, null, null, null, dateTime, km);
        transit.publishAt(dateTime);
        return transit;
    }

    private Transit aFailedAssignmentTransit() {
        Transit transit = aCreatedTransit();
        transit.failDriverAssignment();
        return transit;
    }

    private Transit anAcceptedTransit() {
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        Driver driver = new Driver();
        Transit transit = new Transit(null, null, null, null, dateTime, km);
        transit.proposeTo(driver);
        transit.acceptBy(driver, dateTime);
        return transit;
    }

    private Transit aStartedTransit() {
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        Driver driver = new Driver();
        Transit transit = new Transit(null, null, null, null, dateTime, km);
        transit.proposeTo(driver);
        transit.acceptBy(driver, dateTime);
        transit.startAt(dateTime);
        return transit;
    }

    private Transit aCompletedTransit() {
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance km = Distance.ofKm(10.0f);
        Driver driver = new Driver();
        Transit transit = new Transit(null, null, null, null, dateTime, km);
        transit.proposeTo(driver);
        transit.acceptBy(driver, dateTime);
        transit.startAt(dateTime);
        transit.completeAt(dateTime, null, km);
        return transit;
    }
}