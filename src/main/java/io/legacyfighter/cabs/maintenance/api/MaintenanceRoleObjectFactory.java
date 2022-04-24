package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.mainteance.*;
import io.legacyfighter.cabs.party.api.BaseRoleObjectFactory;
import io.legacyfighter.cabs.party.model.party.Party;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class MaintenanceRoleObjectFactory implements BaseRoleObjectFactory<MaintenanceRole> {

    @Override
    public Optional<MaintenanceRole> getRole(String roleName,
                                             Party party) {
        if (Objects.equals(roleName, "GUARANTOR")) {
            return Optional.of(
                    new ConfigurableRole(
                            party,
                            roleName,
                            new FullCoverageStrategy(),
                            new FreeMaintenanceCostStrategy()
                    )
            );
        }

        if (Objects.equals(roleName, "SERVICE")) {
            return Optional.of(
                    new ConfigurableRole(
                            party,
                            roleName,
                            new ContractBasedCoverageStrategy(),
                            new ContractBasedCostStrategy()
                    )
            );
        }

        if (Objects.equals(roleName, "SELF_SERVICE")) {
            return Optional.of(
                    new ConfigurableRole(
                            party,
                            roleName,
                            new SafePartsCoverageStrategy(),
                            new ContractBasedCostStrategy()
                    )
            );
        }

        return Optional.empty();
    }
}
