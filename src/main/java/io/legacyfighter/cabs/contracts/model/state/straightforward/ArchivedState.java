package io.legacyfighter.cabs.contracts.model.state.straightforward;

import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;

public class ArchivedState extends BaseState {

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
        return true;
    }

    @Override
    protected void acquire(DocumentHeader header) {
        // no-op
    }
}
