package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.role.PartyBasedRole;

/**
 * Base class for all commands that are able to handle {@link MaintenanceRequest}
 */
public abstract class MaintenanceRole extends PartyBasedRole {

    public MaintenanceRole(Party party) {
        super(party);
    }

    public abstract MaintenanceResult handle(MaintenanceRequest maintenanceRequest);
}
