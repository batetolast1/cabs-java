package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrRuleTest {

    @Test
    void orRuleReturnsTrueIfOneRuleIsTrue() {
        // given
        Transit transit = new Transit();
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new OrRule(Set.of(
                new PositiveRule(),
                new StatusRule(Transit.Status.DRAFT),
                new NoFurtherThanRule(Distance.ofKm(50), Transit.Status.TRANSIT_TO_PASSENGER)
        )).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isTrue();
    }

    @Test
    void orRuleReturnsFalseIfNoRules() {
        // given
        Transit transit = new Transit();
        Distance distance = Distance.ofKm(5);

        // when
        boolean satisfied = new OrRule(Set.of()).isSatisfied(transit, distance);

        // then
        assertThat(satisfied).isFalse();
    }
}
