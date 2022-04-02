package io.legacyfighter.cabs.entity.contract;

import io.legacyfighter.cabs.common.BaseEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Contract extends BaseEntity {

    public enum Status {
        NEGOTIATIONS_IN_PROGRESS, REJECTED, ACCEPTED
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
    @Fetch(value = FetchMode.JOIN)
    private Set<ContractAttachmentDecision> contractAttachmentDecisions;

    public Contract() {
    }

    public Contract(String partnerName,
                    String subject,
                    String contractNo,
                    Instant creationDate) {
        this.partnerName = partnerName;
        this.subject = subject;
        this.contractNo = contractNo;
        this.creationDate = creationDate;
        this.status = Status.NEGOTIATIONS_IN_PROGRESS;
        this.contractAttachmentDecisions = new HashSet<>();
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

    Set<ContractAttachmentDecision> getContractAttachmentDecisions() {
        return contractAttachmentDecisions;
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
        if (!isEveryContractAttachmentDecisionAcceptedByBothSides()) {
            throw new IllegalStateException("Not all attachments accepted by both sides");
        }

        this.status = Status.ACCEPTED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public UUID proposeAttachment() {
        ContractAttachmentDecision contractAttachmentDecision = new ContractAttachmentDecision(this);
        this.contractAttachmentDecisions.add(contractAttachmentDecision);
        return contractAttachmentDecision.getContractAttachmentNo();
    }

    public void acceptAttachment(UUID contractAttachmentNo) {
        ContractAttachmentDecision contractAttachmentDecision = findContractAttachmentDecision(contractAttachmentNo);

        if (Objects.equals(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE, contractAttachmentDecision.getStatus())
                || Objects.equals(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES, contractAttachmentDecision.getStatus())) {
            contractAttachmentDecision.setStatus(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES);
        } else {
            contractAttachmentDecision.setStatus(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
        }
    }

    public void rejectAttachment(UUID contractAttachmentNo) {
        ContractAttachmentDecision contractAttachmentDecision = findContractAttachmentDecision(contractAttachmentNo);
        contractAttachmentDecision.setStatus(ContractAttachmentDecision.Status.REJECTED);
    }

    public void removeAttachment(UUID contractAttachmentNo) {
        this.contractAttachmentDecisions.removeIf(contractAttachmentDecision -> contractAttachmentDecision.getContractAttachmentNo().equals(contractAttachmentNo));
    }

    public ContractAttachmentDecision findContractAttachmentDecision(UUID contractAttachmentNo) {
        return this.contractAttachmentDecisions
                .stream()
                .filter(contractAttachmentDecision -> Objects.equals(contractAttachmentDecision.getContractAttachmentNo(), contractAttachmentNo))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
    }

    private boolean isEveryContractAttachmentDecisionAcceptedByBothSides() {
        return this.contractAttachmentDecisions.stream()
                .allMatch(contractAttachmentDecision -> Objects.equals(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES, contractAttachmentDecision.getStatus()));
    }

    public List<UUID> getContractAttachmentNos() {
        return this.contractAttachmentDecisions
                .stream()
                .map(ContractAttachmentDecision::getContractAttachmentNo)
                .collect(Collectors.toList());
    }
}
