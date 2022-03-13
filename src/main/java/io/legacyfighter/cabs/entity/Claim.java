package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static io.legacyfighter.cabs.entity.Claim.CompletionMode.AUTOMATIC;
import static io.legacyfighter.cabs.entity.Claim.CompletionMode.MANUAL;
import static io.legacyfighter.cabs.entity.Claim.Status.ESCALATED;
import static io.legacyfighter.cabs.entity.Claim.Status.REFUNDED;

@Entity
public class Claim extends BaseEntity {

    public enum Status {

        DRAFT, NEW, IN_PROCESS, REFUNDED, ESCALATED, REJECTED
    }

    public enum CompletionMode {

        MANUAL, AUTOMATIC
    }

    public Claim() {
    }

    @ManyToOne
    private Client owner;

    @OneToOne
    private Transit transit;

    @Column(nullable = false)
    private Instant creationDate;

    private Instant completionDate;

    private Instant changeDate;

    @Column(nullable = false)
    private String reason;

    private String incidentDescription;

    @Enumerated(EnumType.STRING)
    private CompletionMode completionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private String claimNo;

    public String getClaimNo() {
        return claimNo;
    }

    public void setClaimNo(String claimNo) {
        this.claimNo = claimNo;
    }

    public Client getOwner() {
        return owner;
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }

    public Transit getTransit() {
        return transit;
    }

    public void setTransit(Transit transit) {
        this.transit = transit;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Instant completionDate) {
        this.completionDate = completionDate;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public CompletionMode getCompletionMode() {
        return completionMode;
    }

    public void setCompletionMode(CompletionMode completionMode) {
        this.completionMode = completionMode;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void escalate() {
        this.setStatus(ESCALATED);
        this.setCompletionMode(MANUAL);
        this.setCompletionDate(Instant.now());
        this.setChangeDate(Instant.now());
    }

    public void refund() {
        this.setStatus(REFUNDED);
        this.setCompletionMode(AUTOMATIC);
        this.setCompletionDate(Instant.now());
        this.setChangeDate(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Claim))
            return false;

        Claim other = (Claim) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, transit, creationDate, completionDate, changeDate, reason, incidentDescription, completionMode, status, claimNo);
    }
}
