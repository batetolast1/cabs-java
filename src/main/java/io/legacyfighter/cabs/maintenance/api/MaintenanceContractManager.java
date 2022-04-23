package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.dict.PartyRelationshipsDictionary;
import io.legacyfighter.cabs.maintenance.model.dict.PartyRolesDictionary;
import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.party.PartyRelationshipRepository;
import io.legacyfighter.cabs.party.model.party.PartyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceContractManager {

    private final PartyRepository partyRepository;

    private final PartyRelationshipRepository partyRelationshipRepository;

    MaintenanceContractManager(PartyRepository partyRepository,
                               PartyRelationshipRepository partyRelationshipRepository) {
        this.partyRepository = partyRepository;
        this.partyRelationshipRepository = partyRelationshipRepository;
    }

    @Transactional
    public void asoMaintenanceRegistered(PartyId distributorId, PartyId vehicleId) {
        Party distributor = partyRepository.put(distributorId.toUUID());
        Party vehicle = partyRepository.put(vehicleId.toUUID());

        partyRelationshipRepository.put(
                PartyRelationshipsDictionary.MAINTENANCE.toString(),
                PartyRolesDictionary.GUARANTOR.getRoleName(), distributor,
                PartyRolesDictionary.CUSTOMER.getRoleName(), vehicle
        );
    }

    @Transactional
    public void thirdPartyMaintenanceContractSigned(PartyId serviceId, PartyId vehicleId) {
        Party service = partyRepository.put(serviceId.toUUID());
        Party vehicle = partyRepository.put(vehicleId.toUUID());

        partyRelationshipRepository.put(
                PartyRelationshipsDictionary.MAINTENANCE.toString(),
                PartyRolesDictionary.SERVICE.getRoleName(), service,
                PartyRolesDictionary.LESSEE.getRoleName(), vehicle
        );
    }

    @Transactional
    public void selfServiceMaintenanceContractSigned(PartyId ownerId, PartyId vehicleId) {
        Party owner = partyRepository.put(ownerId.toUUID());
        Party vehicle = partyRepository.put(vehicleId.toUUID());

        partyRelationshipRepository.put(
                PartyRelationshipsDictionary.MAINTENANCE.toString(),
                PartyRolesDictionary.SELF_SERVICE.getRoleName(), owner,
                PartyRolesDictionary.OWNER.getRoleName(), vehicle
        );
    }
}
