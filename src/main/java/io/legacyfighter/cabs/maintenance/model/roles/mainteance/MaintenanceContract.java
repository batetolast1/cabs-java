package io.legacyfighter.cabs.maintenance.model.roles.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Map;
import java.util.Set;

public class MaintenanceContract {

    private final Map<MaintenancePartsDictionary, Money> costForCoveredParts;

    private final Double coverageRatio;

    public MaintenanceContract(Map<MaintenancePartsDictionary, Money> costForCoveredParts) {
        this.costForCoveredParts = costForCoveredParts;
        this.coverageRatio = 100.0;
    }

    public MaintenanceContract(Map<MaintenancePartsDictionary, Money> costForCoveredParts,
                               double coverageRatio) {
        this.costForCoveredParts = costForCoveredParts;
        this.coverageRatio = coverageRatio;
    }

    public Money getCostFor(MaintenancePartsDictionary maintenancePart) {
        return this.costForCoveredParts.get(maintenancePart);
    }

    public Set<MaintenancePartsDictionary> getCoveredParts() {
        return this.costForCoveredParts.keySet();
    }

    public double getCoverageRatio() {
        return this.coverageRatio;
    }
}
