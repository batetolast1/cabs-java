package io.legacyfighter.cabs.offers.model.state.config.predicates.statechange;

import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.offers.model.OfferHeader;
import io.legacyfighter.cabs.offers.model.state.config.actions.SetApprovingBy;

import java.util.function.BiPredicate;

public class ApprovalVerifier implements BiPredicate<State, ChangeCommand> {

    public static final String PARAM_APPROVING_BY_ID = SetApprovingBy.PARAM_APPROVING_BY_ID;

    @Override
    public boolean test(State state, ChangeCommand command) {
        return command.getParam(PARAM_APPROVING_BY_ID, Long.class) != null
                && ((OfferHeader) state.getDocumentHeader()).getApprovingId() == null;
    }
}
