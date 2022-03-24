package io.legacyfighter.cabs.entity.contract;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Contract extends BaseEntity {

    public enum Status {
        NEGOTIATIONS_IN_PROGRESS, REJECTED, ACCEPTED;
    }

    private String partnerName;

    private String subject;

    @Column(nullable = false)
    private String contractNo;

    @Column(nullable = false)
    private Instant creationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Instant changeDate;

    private Instant acceptedAt;

    private Instant rejectedAt;

    @OneToMany(mappedBy = "contract",
            cascade = CascadeType.ALL)
    private Set<ContractAttachment> attachments;

    public Contract() {
    }

    public Contract(String partnerName, String subject, String contractNo) {
        this.partnerName = partnerName;
        this.subject = subject;
        this.contractNo = contractNo;
        this.creationDate = Instant.now();
        this.status = Status.NEGOTIATIONS_IN_PROGRESS;
        this.attachments = new HashSet<>();
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getSubject() {
        return subject;
    }

    public String getContractNo() {
        return contractNo;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getChangeDate() {
        return changeDate;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    Set<ContractAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Contract))
            return false;

        Contract other = (Contract) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }

    public void accept() {
        if (!isEveryAttachmentAcceptedByBothSides()) {
            throw new IllegalStateException("Not all attachments accepted by both sides");
        }

        this.status = Status.ACCEPTED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public ContractAttachment proposeAttachment(byte[] data) {
        ContractAttachment attachment = new ContractAttachment(this, data);
        this.attachments.add(attachment);
        return attachment;
    }

    public void acceptAttachment(Long attachmentId) {
        ContractAttachment attachment = findAttachment(attachmentId);

        if (Objects.equals(ContractAttachment.Status.ACCEPTED_BY_ONE_SIDE, attachment.getStatus())
                || Objects.equals(ContractAttachment.Status.ACCEPTED_BY_BOTH_SIDES, attachment.getStatus())) {
            attachment.setStatus(ContractAttachment.Status.ACCEPTED_BY_BOTH_SIDES);
        } else {
            attachment.setStatus(ContractAttachment.Status.ACCEPTED_BY_ONE_SIDE);
        }
    }

    public void rejectAttachment(Long attachmentId) {
        ContractAttachment attachment = findAttachment(attachmentId);
        attachment.setStatus(ContractAttachment.Status.REJECTED);
    }

    private boolean isEveryAttachmentAcceptedByBothSides() {
        return this.attachments.stream()
                .allMatch(attachment -> Objects.equals(ContractAttachment.Status.ACCEPTED_BY_BOTH_SIDES, attachment.getStatus()));
    }

    ContractAttachment findAttachment(Long attachmentId) {
        return this.attachments
                .stream()
                .filter(attachment -> Objects.equals(attachment.getId(), attachmentId))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
    }
}
