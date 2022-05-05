package io.legacyfighter.cabs.document.model;

import io.legacyfighter.cabs.document.model.content.DocumentNumber;

public interface DocumentHeader {

    String getStateDescriptor();

    void setStateDescriptor(String descriptor);

    void changeCurrentContent(ContentId contentId);

    Long getId();

    DocumentNumber getDocumentNumber();

    ContentId getContentId();
}
