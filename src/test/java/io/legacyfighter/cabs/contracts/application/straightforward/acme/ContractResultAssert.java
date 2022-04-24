package io.legacyfighter.cabs.contracts.application.straightforward.acme;

import io.legacyfighter.cabs.contracts.application.acme.straigthforward.ContractResult;
import io.legacyfighter.cabs.contracts.model.content.DocumentNumber;
import io.legacyfighter.cabs.contracts.model.state.straightforward.BaseState;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractResultAssert {

    private final ContractResult result;

    public ContractResultAssert(ContractResult result) {
        this.result = result;
    }

    public ContractResultAssert success() {
        assertThat(result.getResult()).isEqualTo(ContractResult.Result.SUCCESS);

        return this;
    }

    public ContractResultAssert failure() {
        assertThat(result.getResult()).isEqualTo(ContractResult.Result.FAILURE);

        return this;
    }

    public ContractResultAssert state(BaseState state) {
        assertThat(result.getStateDescriptor()).isEqualTo(state.getStateDescriptor());

        return this;
    }

    public ContractResultAssert documentHeader(Long documentHeaderId) {
        assertThat(result.getDocumentHeaderId()).isEqualTo(documentHeaderId);

        return this;
    }

    public ContractResultAssert documentNumber(DocumentNumber documentNumber) {
        assertThat(result.getDocumentNumber()).isEqualTo(documentNumber);

        return this;
    }
}
