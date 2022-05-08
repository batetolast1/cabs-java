package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;

public class PositiveRule implements Rule {

    @Override
    public boolean isSatisfied(Transit transit, Distance newDistance) {
        return true;
    }
}
