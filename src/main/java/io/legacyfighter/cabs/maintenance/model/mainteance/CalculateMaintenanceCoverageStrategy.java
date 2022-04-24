package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;

import java.util.Set;

public interface CalculateMaintenanceCoverageStrategy {

    Set<MaintenancePartsDictionary> checkCoverage(Set<MaintenancePartsDictionary> parts,
                                                  MaintenanceContract contract);
}
