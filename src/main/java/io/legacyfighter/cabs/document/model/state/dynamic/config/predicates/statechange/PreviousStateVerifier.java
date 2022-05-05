package io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange;

import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;

import java.util.function.BiPredicate;

public class PreviousStateVerifier implements BiPredicate<State, ChangeCommand> {

    private final String stateDescriptor;

    public PreviousStateVerifier(String stateDescriptor) {
        this.stateDescriptor = stateDescriptor;
    }

    @Override
    public boolean test(State state, ChangeCommand command) {
        return state.getStateDescriptor().equals(stateDescriptor);
    }
}
