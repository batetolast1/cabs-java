package io.legacyfighter.cabs.offers.model;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class OfferHeader extends BaseEntity implements DocumentHeader {

    @Embedded
    private DocumentNumber number;

    private Long authorId;

    private String stateDescriptor;

    @Embedded
    private ContentId contentId;

    private Long approvingId;

    private Long rejectedById;

    protected OfferHeader() {
    }

    public OfferHeader(Long authorId, DocumentNumber number) {
        this.authorId = authorId;
        this.number = number;
    }

    public void changeCurrentContent(ContentId contentId) {
        this.contentId = contentId;
    }

    public boolean notEmpty() {
        return contentId != null;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getStateDescriptor() {
        return stateDescriptor;
    }

    public void setStateDescriptor(String stateDescriptor) {
        this.stateDescriptor = stateDescriptor;
    }

    public DocumentNumber getDocumentNumber() {
        return number;
    }

    public ContentId getContentId() {
        return contentId;
    }

    public Long getApprovingId() {
        return approvingId;
    }

    public void setApprovingId(Long approvingId) {
        this.approvingId = approvingId;
    }

    public Long getRejectedById() {
        return rejectedById;
    }

    public void setRejectedById(Long rejectedById) {
        this.rejectedById = rejectedById;
    }
}
