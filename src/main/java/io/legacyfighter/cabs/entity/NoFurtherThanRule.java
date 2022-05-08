package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;

public class NoFurtherThanRule implements Rule {

    private final Distance limit;

    private final Transit.Status status;

    public NoFurtherThanRule(Distance limit, Transit.Status status) {
        this.limit = limit;
        this.status = status;
    }

    @Override
    public boolean isSatisfied(Transit transit, Distance newDistance) {
        if (transit.getStatus() != this.status) {
            return false;
        }

        return Distance.between(transit.getKm(), newDistance).toKmInFloat() <= this.limit.toKmInFloat();
    }
}
