package io.legacyfighter.cabs.entity.contract;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class ContractAttachmentDecision extends BaseEntity {

    public enum Status {
        PROPOSED, ACCEPTED_BY_ONE_SIDE, ACCEPTED_BY_BOTH_SIDES, REJECTED
    }

    @Column(nullable = false)
    private UUID contractAttachmentNo;

    @ManyToOne
    private Contract contract;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant acceptedAt;

    private Instant rejectedAt;

    private Instant changeDate;

    public ContractAttachmentDecision() {
    }

    public ContractAttachmentDecision(Contract contract) {
        this.contract = contract;
        this.contractAttachmentNo = UUID.randomUUID();
        this.status = Status.PROPOSED;
    }

    public UUID getContractAttachmentNo() {
        return contractAttachmentNo;
    }

    public Contract getContract() {
        return contract;
    }

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Instant getChangeDate() {
        return changeDate;
    }

    void setChangeDate(Instant changeDate) {
        this.changeDate = changeDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ContractAttachmentDecision))
            return false;

        ContractAttachmentDecision other = (ContractAttachmentDecision) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
