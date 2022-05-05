package io.legacyfighter.cabs.contracts.application.straightforward;

import io.legacyfighter.cabs.document.model.content.DocumentNumber;

public class ContractResult {

    public enum Result {
        FAILURE, SUCCESS
    }

    private final Result result;
    private final Long documentHeaderId;
    private final DocumentNumber documentNumber;
    private final String stateDescriptor;

    public ContractResult(Result result,
                          Long documentHeaderId,
                          DocumentNumber documentNumber,
                          String stateDescriptor) {
        this.result = result;
        this.documentHeaderId = documentHeaderId;
        this.documentNumber = documentNumber;
        this.stateDescriptor = stateDescriptor;
    }

    public Result getResult() {
        return result;
    }

    public DocumentNumber getDocumentNumber() {
        return documentNumber;
    }

    public Long getDocumentHeaderId() {
        return documentHeaderId;
    }

    public String getStateDescriptor() {
        return stateDescriptor;
    }
}
