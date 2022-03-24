package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.contract.ContractAttachmentData;
import io.legacyfighter.cabs.entity.contract.ContractAttachmentDecision;

import java.time.Instant;

public class ContractAttachmentDTO {

    private Long id;

    private Long contractId;

    private byte[] data;

    private Instant creationDate;

    private Instant acceptedAt;

    private Instant rejectedAt;

    private Instant changeDate;

    private ContractAttachmentDecision.Status status;

    public ContractAttachmentDTO() {
    }

    public ContractAttachmentDTO(ContractAttachmentDecision attachment, ContractAttachmentData data) {
        this.id = attachment.getId();
        this.data = data.getData();
        this.contractId = attachment.getContract().getId();
        this.creationDate = data.getCreationDate();
        this.rejectedAt = attachment.getRejectedAt();
        this.acceptedAt = attachment.getAcceptedAt();
        this.changeDate = attachment.getChangeDate();
        this.status = attachment.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Instant getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Instant changeDate) {
        this.changeDate = changeDate;
    }

    public ContractAttachmentDecision.Status getStatus() {
        return status;
    }

    public void setStatus(ContractAttachmentDecision.Status status) {
        this.status = status;
    }
}
