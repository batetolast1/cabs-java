package io.legacyfighter.cabs.repair.model.roles.repair;

import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.role.PartyBasedRole;

/**
 * Base class for all commands that are able to handle {@link RepairRequest}
 */
public abstract class RoleForRepairer extends PartyBasedRole {

    public RoleForRepairer(Party party) {
        super(party);
    }

    public abstract RepairingResult handle(RepairRequest repairRequest);
}