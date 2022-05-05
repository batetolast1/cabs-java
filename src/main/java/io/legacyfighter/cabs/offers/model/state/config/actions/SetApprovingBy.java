package io.legacyfighter.cabs.offers.model.state.config.actions;

import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.offers.model.OfferHeader;

import java.util.function.BiConsumer;

public class SetApprovingBy implements BiConsumer<DocumentHeader, ChangeCommand> {

    public static final String PARAM_APPROVING_BY_ID = "approving";

    @Override
    public void accept(DocumentHeader contractHeader, ChangeCommand command) {
        ((OfferHeader) contractHeader).setApprovingId(command.getParam(PARAM_APPROVING_BY_ID, Long.class));
    }
}
