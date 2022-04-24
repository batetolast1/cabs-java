package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.dict.PartyRelationshipsDictionary;
import io.legacyfighter.cabs.maintenance.model.mainteance.MaintenanceRequest;
import io.legacyfighter.cabs.party.api.PartyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceProcess {

    private final PartyMapper partyMapper;

    private final MaintenanceRoleObjectFactory maintenanceRoleObjectFactory;

    @Autowired
    public MaintenanceProcess(PartyMapper partyMapper,
                              MaintenanceRoleObjectFactory maintenanceRoleObjectFactory) {
        this.partyMapper = partyMapper;
        this.maintenanceRoleObjectFactory = maintenanceRoleObjectFactory;
    }

    public MaintenanceResolveResult resolve(MaintenanceRequest maintenanceRequest) {
        return partyMapper.mapRelation(maintenanceRequest.getVehicle(), PartyRelationshipsDictionary.MAINTENANCE.name())
                .flatMap(partyRelationship -> maintenanceRoleObjectFactory.getRole(partyRelationship.getRoleA(), partyRelationship.getPartyA()))
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
