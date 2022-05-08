package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositiveRuleTest {

    @Test
    void positiveRuleReturnsTrue() {
        // given
        Transit transit = new Transit();
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new PositiveRule().isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isTrue();
    }
}
