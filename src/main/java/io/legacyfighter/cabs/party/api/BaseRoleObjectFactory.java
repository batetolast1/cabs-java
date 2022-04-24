package io.legacyfighter.cabs.party.api;

import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.role.PartyBasedRole;

import java.util.Optional;

public interface BaseRoleObjectFactory<T extends PartyBasedRole> {

    Optional<T> getRole(String roleName, Party party);
}
