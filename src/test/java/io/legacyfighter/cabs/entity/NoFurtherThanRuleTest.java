package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NoFurtherThanRuleTest {

    @Test
    void noFurtherThanRuleReturnsTrueIfConditionsAreMet() {
        // given
        Transit transit = new Transit();
        transit.publishAt(Instant.now());
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new NoFurtherThanRule(Distance.ofKm(10), Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isTrue();
    }

    @Test
    void noFurtherThanRuleReturnsFalseIfDistanceIsTooBig() {
        // given
        Transit transit = new Transit();
        transit.publishAt(Instant.now());
        Distance distance = Distance.ofKm(15);

        // when
        boolean satisfied = new NoFurtherThanRule(Distance.ofKm(10), Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isFalse();
    }

    @Test
    void noFurtherThanRuleReturnsFalseIfStatusIsNotValid() {
        // given
        Transit transit = new Transit();
        transit.publishAt(Instant.now());
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new NoFurtherThanRule(Distance.ofKm(10), Transit.Status.DRAFT).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isFalse();
    }
}
