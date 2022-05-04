package io.legacyfighter.cabs.contracts.model.state.dynamic.config.actions;

import io.legacyfighter.cabs.contracts.model.DocumentHeader;
import io.legacyfighter.cabs.contracts.model.state.dynamic.ChangeCommand;

import java.util.function.BiConsumer;

public class ChangeVerifier implements BiConsumer<DocumentHeader, ChangeCommand> {

    public static final String PARAM_VERIFIER = "verifier";

    @Override
    public void accept(DocumentHeader documentHeader, ChangeCommand command) {
        documentHeader.setVerifierId(command.getParam(PARAM_VERIFIER, Long.class));
    }
}
