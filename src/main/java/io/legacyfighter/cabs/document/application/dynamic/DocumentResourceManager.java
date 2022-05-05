package io.legacyfighter.cabs.document.application.dynamic;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;

import java.util.Map;

public interface DocumentResourceManager<T extends DocumentHeader> {

    DocumentOperationResult createDocument(Long authorId);

    DocumentOperationResult changeState(Long documentId, String desiredState, Map<String, Object> params);

    DocumentOperationResult changeContent(Long headerId, ContentId contentVersion);
}
