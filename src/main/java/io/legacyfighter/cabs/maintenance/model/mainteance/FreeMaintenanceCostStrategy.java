package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Set;

public class FreeMaintenanceCostStrategy implements CalculateMaintenanceCostStrategy {

    @Override
    public Money calculateCost(Set<MaintenancePartsDictionary> parts, MaintenanceContract contract) {
        return Money.ZERO;
    }
}
