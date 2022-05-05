package io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange;

import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;

import java.util.function.BiPredicate;

public class PositiveVerifier implements BiPredicate<State, ChangeCommand> {

    @Override
    public boolean test(State state, ChangeCommand command) {
        return true;
    }
}
