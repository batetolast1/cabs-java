package io.legacyfighter.cabs.maintenance.model.roles.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.party.api.PartyId;

import java.util.Set;

public class MaintenanceRequest {

    private final PartyId vehicle;

    private final Set<MaintenancePartsDictionary> maintenanceParts;

    private final MaintenanceContract maintenanceContract;

    public MaintenanceRequest(PartyId vehicle,
                              Set<MaintenancePartsDictionary> maintenanceParts,
                              MaintenanceContract maintenanceContract) {
        this.vehicle = vehicle;
        this.maintenanceParts = maintenanceParts;
        this.maintenanceContract = maintenanceContract;
    }

    public PartyId getVehicle() {
        return this.vehicle;
    }

    public Set<MaintenancePartsDictionary> getMaintenanceParts() {
        return maintenanceParts;
    }

    public MaintenanceContract getMaintenanceContract() {
        return this.maintenanceContract;
    }
}
