package io.legacyfighter.cabs.common;

import io.legacyfighter.cabs.document.application.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.document.model.ContentId;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentOperationResultAssert {

    private final DocumentOperationResult result;

    public DocumentOperationResultAssert(DocumentOperationResult result) {
        this.result = result;
        assertThat(result.getResult()).isEqualTo(DocumentOperationResult.Result.SUCCESS);
    }

    public DocumentOperationResultAssert editable() {
        assertThat(result.isContentChangePossible()).isTrue();
        return this;
    }

    public DocumentOperationResultAssert nonEditable() {
        assertThat(result.isContentChangePossible()).isFalse();
        return this;
    }

    public DocumentOperationResultAssert state(String state) {
        assertThat(state).isEqualTo(result.getStateName());
        return this;
    }

    public DocumentOperationResultAssert content(ContentId contentId) {
        assertThat(contentId).isEqualTo(result.getContentId());
        return this;
    }

    public DocumentOperationResultAssert possibleNextStates(String... states) {
        assertThat(Set.of(states)).containsExactlyInAnyOrderElementsOf(result.getPossibleTransitionsAndRules().keySet());
        return this;
    }

    public DocumentOperationResultAssert noPossibleNextStates() {
        assertThat(result.getPossibleTransitionsAndRules()).isEmpty();
        return this;
    }
}
