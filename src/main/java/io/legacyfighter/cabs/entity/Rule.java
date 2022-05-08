package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;

public interface Rule {

    boolean isSatisfied(Transit transit, Distance newDistance);
}
