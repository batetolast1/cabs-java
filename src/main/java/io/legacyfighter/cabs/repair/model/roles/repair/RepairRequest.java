package io.legacyfighter.cabs.repair.model.roles.repair;

import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.repair.legacy.parts.Parts;

import java.util.Set;

public class RepairRequest {

    private final PartyId vehicle;

    private final Set<Parts> partsToRepair;

    public RepairRequest(PartyId vehicle, Set<Parts> parts) {
        this.vehicle = vehicle;
        this.partsToRepair = parts;
    }

    public Set<Parts> getPartsToRepair() {
        return partsToRepair;
    }

    public PartyId getVehicle() {
        return vehicle;
    }
}
