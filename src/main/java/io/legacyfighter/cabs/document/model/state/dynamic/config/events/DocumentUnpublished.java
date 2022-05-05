package io.legacyfighter.cabs.document.model.state.dynamic.config.events;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;

public class DocumentUnpublished extends DocumentEvent {

    public DocumentUnpublished(Long documentId, String currentSate, ContentId contentId, DocumentNumber number) {
        super(documentId, currentSate, contentId, number);
    }
}
