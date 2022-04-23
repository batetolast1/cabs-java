package io.legacyfighter.cabs.maintenance.model.roles.empty;

import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.role.PartyBasedRole;

public class Owner extends PartyBasedRole {

    public Owner(Party party) {
        super(party);
    }
}
