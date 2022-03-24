package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.entity.contract.Contract.Status;
import io.legacyfighter.cabs.entity.contract.ContractAttachment;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class ContractDTO {

    private Long id;
    private String partnerName;


    private String subject;

    private String contractNo;

    private Instant creationDate;

    private Status status;

    private Instant changeDate;

    private Instant acceptedAt;

    private Instant rejectedAt;

    private Set<ContractAttachmentDTO> attachments = new HashSet<>();

    public ContractDTO() {
    }

    public ContractDTO(Contract contract, Set<ContractAttachment> contractAttachments) {
        this.setContractNo(contract.getContractNo());
        this.setAcceptedAt(contract.getAcceptedAt());
        this.setRejectedAt(contract.getRejectedAt());
        this.setCreationDate(contract.getCreationDate());
        this.setChangeDate(contract.getChangeDate());
        this.setStatus(contract.getStatus());
        this.setPartnerName(contract.getPartnerName());
        this.setSubject(contract.getSubject());
        for (ContractAttachment attachment : contractAttachments) {
            this.attachments.add(new ContractAttachmentDTO(attachment));
        }
        this.setId(contract.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Instant changeDate) {
        this.changeDate = changeDate;
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

    public Set<ContractAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<ContractAttachmentDTO> attachments) {
        this.attachments = attachments;
    }
}
