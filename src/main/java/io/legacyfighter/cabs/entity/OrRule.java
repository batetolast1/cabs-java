package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;

import java.util.Set;

public class OrRule implements Rule {

    private final Set<Rule> rules;

    public OrRule(Set<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean isSatisfied(Transit transit, Distance newDistance) {
        return rules.stream().anyMatch(rule -> rule.isSatisfied(transit, newDistance));
    }
}
