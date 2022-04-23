package io.legacyfighter.cabs.maintenance.model.roles.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Set;
import java.util.UUID;

public class MaintenanceResult {

    private final UUID handlingParty;

    private final Money totalCost;

    private final Set<MaintenancePartsDictionary> handledParts;

    public MaintenanceResult(UUID handlingParty,
                             Money totalCost,
                             Set<MaintenancePartsDictionary> handledParts) {
        this.handlingParty = handlingParty;
        this.totalCost = totalCost;
        this.handledParts = handledParts;
    }

    public UUID getHandlingParty() {
        return handlingParty;
    }

    public Money getTotalCost() {
        return totalCost;
    }

    public Set<MaintenancePartsDictionary> getHandledParts() {
        return handledParts;
    }
}
