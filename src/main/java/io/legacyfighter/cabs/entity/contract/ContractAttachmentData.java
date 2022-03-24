package io.legacyfighter.cabs.entity.contract;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.time.Instant;
import java.util.UUID;

@Entity
public class ContractAttachmentData extends BaseEntity {

    @Column(nullable = false)
    private UUID contractAttachmentNo;

    @Lob
    @Column(name = "data",
            columnDefinition = "BLOB")
    private byte[] data;

    @Column(nullable = false)
    private Instant creationDate;

    public ContractAttachmentData() {
    }

    public ContractAttachmentData(UUID contractAttachmentNo, byte[] data) {
        this.contractAttachmentNo = contractAttachmentNo;
        this.data = data;
        this.creationDate = Instant.now();
    }

    public UUID getContractAttachmentNo() {
        return contractAttachmentNo;
    }

    public byte[] getData() {
        return data;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ContractAttachmentData))
            return false;

        ContractAttachmentData other = (ContractAttachmentData) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
