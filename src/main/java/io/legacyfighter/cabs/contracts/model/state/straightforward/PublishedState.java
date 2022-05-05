package io.legacyfighter.cabs.contracts.model.state.straightforward;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;

public class PublishedState extends BaseState {

    @Override
    protected boolean canChangeContent() {
        return false;
    }

    @Override
    protected BaseState stateAfterContentChange() {
        return this;
    }

    @Override
    protected boolean canChangeFrom(BaseState previousState) {
        return previousState instanceof VerifiedState
                && previousState.getDocumentHeader() instanceof ContractHeader
                && ((ContractHeader) previousState.getDocumentHeader()).notEmpty();
    }

    @Override
    protected void acquire(DocumentHeader header) {
        // no-op
    }
}
