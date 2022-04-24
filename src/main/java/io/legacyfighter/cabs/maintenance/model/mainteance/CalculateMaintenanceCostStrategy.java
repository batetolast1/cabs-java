package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Set;

public interface CalculateMaintenanceCostStrategy {

    Money calculateCost(Set<MaintenancePartsDictionary> parts,
                        MaintenanceContract contract);
}
