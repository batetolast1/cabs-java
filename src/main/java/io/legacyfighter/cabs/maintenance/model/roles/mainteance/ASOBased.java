package io.legacyfighter.cabs.maintenance.model.roles.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.party.model.party.Party;

import java.util.Set;
import java.util.stream.Collectors;

public class ASOBased extends MaintenanceRole {

    public ASOBased(Party party) {
        super(party);
    }

    @Override
    public MaintenanceResult handle(MaintenanceRequest maintenanceRequest) {
        Set<MaintenancePartsDictionary> handledParts = maintenanceRequest.getMaintenanceParts().stream()
                .filter(part -> maintenanceRequest.getMaintenanceContract().getCoveredParts().contains(part))
                .collect(Collectors.toSet());

        return new MaintenanceResult(party.getId(), Money.ZERO, handledParts);
    }
}
