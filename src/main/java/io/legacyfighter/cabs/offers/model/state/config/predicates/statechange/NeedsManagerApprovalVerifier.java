package io.legacyfighter.cabs.offers.model.state.config.predicates.statechange;

import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.offers.model.OfferHeader;

import java.util.function.BiPredicate;

public class NeedsManagerApprovalVerifier implements BiPredicate<State, ChangeCommand> {

    public static final String PARAM_TOTAL_PRICE = "total price";

    private final Money priceLimit;

    public NeedsManagerApprovalVerifier(Money priceLimit) {
        this.priceLimit = priceLimit;
    }

    @Override
    public boolean test(State state, ChangeCommand command) {
        return (command.getParam(PARAM_TOTAL_PRICE, Money.class) != null
                && command.getParam(PARAM_TOTAL_PRICE, Money.class).isLessThan(priceLimit))
                || ((OfferHeader) state.getDocumentHeader()).getApprovingId() != null;
    }
}
