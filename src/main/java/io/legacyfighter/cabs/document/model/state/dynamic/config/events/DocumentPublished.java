package io.legacyfighter.cabs.document.model.state.dynamic.config.events;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;

public class DocumentPublished extends DocumentEvent {

    public DocumentPublished(Long documentId, String currentSate, ContentId contentId, DocumentNumber number) {
        super(documentId, currentSate, contentId, number);
    }
}
