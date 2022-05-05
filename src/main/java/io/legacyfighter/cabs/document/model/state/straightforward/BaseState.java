package io.legacyfighter.cabs.document.model.state.straightforward;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;

//TODO introduce an interface

public abstract class BaseState {

    protected DocumentHeader documentHeader;

    public DocumentHeader getDocumentHeader() {
        return documentHeader;
    }

    public void init(DocumentHeader header) {
        this.documentHeader = header;

        header.setStateDescriptor(getStateDescriptor());
    }

    public BaseState changeContent(ContentId currentContent) {
        if (canChangeContent()) {
            BaseState newState = stateAfterContentChange();
            newState.init(documentHeader);

            this.documentHeader.changeCurrentContent(currentContent);

            newState.acquire(documentHeader);

            return newState;
        }

        return this;
    }

    protected abstract boolean canChangeContent();

    protected abstract BaseState stateAfterContentChange();

    public BaseState changeState(BaseState newState) {
        if (newState.canChangeFrom(this)) {
            newState.init(documentHeader);

            documentHeader.setStateDescriptor(newState.getStateDescriptor());

            newState.acquire(documentHeader);

            return newState;
        }

        return this;
    }

    public String getStateDescriptor() {
        return this.getClass().getName();
    }

    /**
     * template method that allows to perform addition actions during state change
     */
    protected abstract void acquire(DocumentHeader header);

    protected abstract boolean canChangeFrom(BaseState previousState);
}
