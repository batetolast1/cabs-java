package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class StatusRuleTest {

    @Test
    void statusRuleReturnsTrueWhenCorrectStatus() {
        // given
        Transit transit = new Transit();
        transit.publishAt(Instant.now());
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new StatusRule(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isTrue();
    }

    @Test
    void statusRuleReturnsFalseWhenIncorrectStatus() {
        // given
        Transit transit = new Transit();
        transit.publishAt(Instant.now());
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new StatusRule(Transit.Status.DRAFT).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isFalse();
    }
}
