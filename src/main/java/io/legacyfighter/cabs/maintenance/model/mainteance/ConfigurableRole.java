package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.party.model.party.Party;

import java.util.Set;

public class ConfigurableRole extends MaintenanceRole {

    private final String roleName;

    private final CalculateMaintenanceCoverageStrategy coverageStrategy;

    private final CalculateMaintenanceCostStrategy costStrategy;

    public ConfigurableRole(Party party,
                            String roleName,
                            CalculateMaintenanceCoverageStrategy coverageStrategy,
                            CalculateMaintenanceCostStrategy costStrategy) {
        super(party);
        this.roleName = roleName;
        this.coverageStrategy = coverageStrategy;
        this.costStrategy = costStrategy;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public MaintenanceResult handle(MaintenanceRequest maintenanceRequest) {
        Set<MaintenancePartsDictionary> coveredParts = coverageStrategy.checkCoverage(maintenanceRequest.getMaintenanceParts(), maintenanceRequest.getMaintenanceContract());
        Money totalCost = costStrategy.calculateCost(coveredParts, maintenanceRequest.getMaintenanceContract());
        return new MaintenanceResult(this.party.getId(), totalCost, coveredParts);
    }
}
