package io.legacyfighter.cabs.maintenance.model.dict;

import io.legacyfighter.cabs.maintenance.model.roles.empty.Customer;
import io.legacyfighter.cabs.maintenance.model.roles.empty.Lessor;
import io.legacyfighter.cabs.maintenance.model.roles.empty.Owner;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.ASOBased;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.OwnerBased;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.ThirdPartyBased;
import io.legacyfighter.cabs.party.model.role.PartyBasedRole;

/**
 * Enum that emulates database dictionary
 */
public enum PartyRolesDictionary {

    GUARANTOR(ASOBased.class), CUSTOMER(Customer.class),
    SERVICE(ThirdPartyBased.class), LESSEE(Lessor.class),
    SELF_SERVICE(OwnerBased.class), OWNER(Owner.class);

    private final String name;

    PartyRolesDictionary(Class<? extends PartyBasedRole> clazz) {
        name = clazz.getName();
    }

    public String getRoleName() {
        return name;
    }
}
