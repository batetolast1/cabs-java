package io.legacyfighter.cabs.maintenance.model.roles.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.party.model.party.Party;

import java.util.Set;
import java.util.stream.Collectors;

import static io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary.*;

public class OwnerBased extends MaintenanceRole {

    public OwnerBased(Party party) {
        super(party);
    }

    @Override
    public MaintenanceResult handle(MaintenanceRequest maintenanceRequest) {
        Set<MaintenancePartsDictionary> handledParts = maintenanceRequest.getMaintenanceParts().stream()
                .filter(part -> maintenanceRequest.getMaintenanceContract().getCoveredParts().contains(part))
                .collect(Collectors.toSet());

        handledParts.removeAll(Set.of(ENGINE, GEARBOX, SUSPENSION, CAR_OIL, CAR_UPHOLSTERY));

        Money totalCost = handledParts.stream()
                .map(part -> maintenanceRequest.getMaintenanceContract().getCostFor(part))
                .reduce(Money.ZERO, Money::add)
                .percentage(maintenanceRequest.getMaintenanceContract().getCoverageRatio());

        return new MaintenanceResult(party.getId(), totalCost, handledParts);
    }
}
