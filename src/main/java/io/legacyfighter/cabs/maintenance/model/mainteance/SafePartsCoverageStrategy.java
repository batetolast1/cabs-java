package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;

import java.util.Set;
import java.util.stream.Collectors;

import static io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary.*;

public class SafePartsCoverageStrategy implements CalculateMaintenanceCoverageStrategy {

    private static final Set<MaintenancePartsDictionary> UNSAFE_PARTS = Set.of(ENGINE, GEARBOX, SUSPENSION, CAR_OIL, CAR_UPHOLSTERY);

    @Override
    public Set<MaintenancePartsDictionary> checkCoverage(Set<MaintenancePartsDictionary> parts,
                                                         MaintenanceContract contract) {
        return parts.stream()
                .filter(contract::isPartCovered)
                .filter(SafePartsCoverageStrategy::isPartSafe)
                .collect(Collectors.toSet());
    }

    private static boolean isPartSafe(MaintenancePartsDictionary part) {
        return !UNSAFE_PARTS.contains(part);
    }
}
