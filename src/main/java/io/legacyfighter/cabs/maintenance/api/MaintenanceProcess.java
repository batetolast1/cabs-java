package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.dict.PartyRelationshipsDictionary;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.MaintenanceRequest;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.MaintenanceRole;
import io.legacyfighter.cabs.party.api.PartyMapper;
import io.legacyfighter.cabs.party.api.RoleObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceProcess {

    private final PartyMapper partyMapper;

    @Autowired
    public MaintenanceProcess(PartyMapper partyMapper) {
        this.partyMapper = partyMapper;
    }

    public MaintenanceResolveResult resolve(MaintenanceRequest maintenanceRequest) {
        return partyMapper.mapRelation(maintenanceRequest.getVehicle(), PartyRelationshipsDictionary.MAINTENANCE.name())
                .map(RoleObjectFactory::from)
                .flatMap(roleObjectFactory -> roleObjectFactory.getRole(MaintenanceRole.class))
                .map(maintenanceRole -> maintenanceRole.handle(maintenanceRequest))
                .map(maintenanceResult ->
                        new MaintenanceResolveResult(
                                MaintenanceResolveResult.Status.SUCCESS,
                                maintenanceResult.getHandlingParty(),
                                maintenanceResult.getTotalCost(),
                                maintenanceResult.getHandledParts()
                        )
                )
                .orElseGet(() -> new MaintenanceResolveResult(MaintenanceResolveResult.Status.ERROR));
    }
}
