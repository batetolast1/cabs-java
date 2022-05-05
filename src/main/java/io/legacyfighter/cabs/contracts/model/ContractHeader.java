package io.legacyfighter.cabs.contracts.model;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class ContractHeader extends BaseEntity implements DocumentHeader {

    @Embedded
    private DocumentNumber number;

    private Long authorId;

    private Long verifierId;

    private String stateDescriptor;

    @Embedded
    private ContentId contentId;

    protected ContractHeader() {
    }

    public ContractHeader(Long authorId, DocumentNumber number) {
        this.authorId = authorId;
        this.number = number;
    }

    public void changeCurrentContent(ContentId contentId) {
        this.contentId = contentId;
    }

    public boolean notEmpty() {
        return contentId != null;
    }

    public Long getVerifier() {
        return verifierId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setVerifierId(Long verifierId) {
        this.verifierId = verifierId;
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
}
