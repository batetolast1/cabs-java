package io.legacyfighter.cabs.entity.contract;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class ContractAttachment extends BaseEntity {

    public enum Status {
        PROPOSED, ACCEPTED_BY_ONE_SIDE, ACCEPTED_BY_BOTH_SIDES, REJECTED;
    }

    @ManyToOne
    private Contract contract;

    @Lob
    @Column(name = "data", columnDefinition = "BLOB")
    private byte[] data;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Instant creationDate;

    private Instant acceptedAt;

    private Instant rejectedAt;

    private Instant changeDate;

    public ContractAttachment() {
    }

    public ContractAttachment(Contract contract, byte[] data) {
        this.contract = contract;
        this.data = data;
        this.status = Status.PROPOSED;
        this.creationDate = Instant.now();
    }

    void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
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

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    public Contract getContract() {
        return contract;
    }

    void setContract(Contract contract) {
        this.contract = contract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ContractAttachment))
            return false;

        ContractAttachment other = (ContractAttachment) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
