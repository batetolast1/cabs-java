package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;

import java.util.Set;
import java.util.stream.Collectors;

public class ContractBasedCoverageStrategy implements CalculateMaintenanceCoverageStrategy {

    @Override
    public Set<MaintenancePartsDictionary> checkCoverage(Set<MaintenancePartsDictionary> parts,
                                                         MaintenanceContract contract) {
        return parts.stream()
                .filter(contract::isPartCovered)
                .collect(Collectors.toSet());
    }
}
