package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;

public class StatusRule implements Rule {

    private final Transit.Status status;

    public StatusRule(Transit.Status status) {
        this.status = status;
    }

    @Override
    public boolean isSatisfied(Transit transit, Distance newDistance) {
        return transit.getStatus() == this.status;
    }
}
