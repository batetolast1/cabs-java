package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Set;
import java.util.UUID;

public class MaintenanceResolveResult {

    public enum Status {
        SUCCESS, ERROR
    }

    private UUID handlingParty;
    private Money totalCost;
    private Set<MaintenancePartsDictionary> acceptedParts;
    private final Status status;

    public MaintenanceResolveResult(Status status,
                                    UUID handlingParty,
                                    Money totalCost,
                                    Set<MaintenancePartsDictionary> acceptedParts) {
        this.status = status;
        this.handlingParty = handlingParty;
        this.totalCost = totalCost;
        this.acceptedParts = acceptedParts;
    }

    public MaintenanceResolveResult(Status status) {
        this.status = status;
    }

    public UUID getHandlingParty() {
        return handlingParty;
    }

    public Money getTotalCost() {
        return totalCost;
    }

    public Status getStatus() {
        return status;
    }

    public Set<MaintenancePartsDictionary> getAcceptedParts() {
        return acceptedParts;
    }
}
