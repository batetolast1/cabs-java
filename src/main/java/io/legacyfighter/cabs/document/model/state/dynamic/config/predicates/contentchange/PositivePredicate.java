package io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.contentchange;

import io.legacyfighter.cabs.document.model.state.dynamic.State;

import java.util.function.Predicate;

public class PositivePredicate implements Predicate<State> {

    @Override
    public boolean test(State state) {
        return true;
    }
}