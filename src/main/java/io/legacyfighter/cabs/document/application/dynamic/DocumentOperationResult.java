package io.legacyfighter.cabs.document.application.dynamic;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;

import java.util.List;
import java.util.Map;

public class DocumentOperationResult {

    public enum Result {
        SUCCESS, ERROR
    }

    private final Result result;
    private final String stateName;
    private final ContentId contentId;
    private final Long documentHeaderId;
    private final DocumentNumber documentNumber;
    private final Map<String, List<String>> possibleTransitionsAndRules;
    private final boolean contentChangePossible;
    private final String contentChangePredicate;

    public DocumentOperationResult(Result result,
                                   Long documentHeaderId,
                                   DocumentNumber documentNumber,
                                   String stateName,
                                   ContentId contentId,
                                   Map<String, List<String>> possibleTransitionsAndRules,
                                   boolean contentChangePossible,
                                   String contentChangePredicate) {
        this.result = result;
        this.documentHeaderId = documentHeaderId;
        this.documentNumber = documentNumber;
        this.stateName = stateName;
        this.contentId = contentId;
        this.possibleTransitionsAndRules = possibleTransitionsAndRules;
        this.contentChangePossible = contentChangePossible;
        this.contentChangePredicate = contentChangePredicate;
    }

    public Map<String, List<String>> getPossibleTransitionsAndRules() {
        return possibleTransitionsAndRules;
    }

    public String getContentChangePredicate() {
        return contentChangePredicate;
    }

    public boolean isContentChangePossible() {
        return contentChangePossible;
    }

    public Result getResult() {
        return result;
    }

    public String getStateName() {
        return stateName;
    }

    public DocumentNumber getDocumentNumber() {
        return documentNumber;
    }

    public Long getDocumentHeaderId() {
        return documentHeaderId;
    }

    public ContentId getContentId() {
        return contentId;
    }
}
