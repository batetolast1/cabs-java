package io.legacyfighter.cabs.contracts.model.state.straightforward;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;

public class VerifiedState extends BaseState {

    private Long verifierId;

    @SuppressWarnings("unused")
    public VerifiedState() {
    }

    public VerifiedState(Long verifierId) {
        this.verifierId = verifierId;
    }

    @Override
    protected boolean canChangeContent() {
        return true;
    }

    @Override
    protected BaseState stateAfterContentChange() {
        return new DraftState();
    }

    @Override
    protected boolean canChangeFrom(BaseState previousState) {
        return previousState instanceof DraftState
                && previousState.getDocumentHeader() instanceof ContractHeader
                && !((ContractHeader) previousState.getDocumentHeader()).getAuthorId().equals(verifierId)
                && ((ContractHeader) previousState.getDocumentHeader()).notEmpty();
    }

    @Override
    protected void acquire(DocumentHeader header) {
        ((ContractHeader) header).setVerifierId(verifierId);
    }
}
