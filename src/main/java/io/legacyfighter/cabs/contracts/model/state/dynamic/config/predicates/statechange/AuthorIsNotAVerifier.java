package io.legacyfighter.cabs.contracts.model.state.dynamic.config.predicates.statechange;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.actions.ChangeVerifier;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;

import java.util.function.BiPredicate;

public class AuthorIsNotAVerifier implements BiPredicate<State, ChangeCommand> {

    public static final String PARAM_VERIFIER = ChangeVerifier.PARAM_VERIFIER;

    @Override
    public boolean test(State state, ChangeCommand command) {
        return state.getDocumentHeader() instanceof ContractHeader
                && !command.getParam(PARAM_VERIFIER, Long.class).equals(((ContractHeader) state.getDocumentHeader()).getAuthorId());
    }
}
