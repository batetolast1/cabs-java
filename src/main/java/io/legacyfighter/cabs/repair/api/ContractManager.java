package io.legacyfighter.cabs.repair.api;

import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.party.PartyRelationshipRepository;
import io.legacyfighter.cabs.party.model.party.PartyRepository;
import io.legacyfighter.cabs.repair.model.dict.PartyRelationshipsDictionary;
import io.legacyfighter.cabs.repair.model.dict.PartyRolesDictionary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContractManager {

    private final PartyRepository partyRepository;

    private final PartyRelationshipRepository partyRelationshipRepository;

    public ContractManager(PartyRepository partyRepository,
                           PartyRelationshipRepository partyRelationshipRepository) {
        this.partyRepository = partyRepository;
        this.partyRelationshipRepository = partyRelationshipRepository;
    }

    public void extendedWarrantyContractSigned(PartyId insurerId, PartyId vehicleId) {
        Party insurer = partyRepository.put(insurerId.toUUID());
        Party vehicle = partyRepository.put(vehicleId.toUUID());

        partyRelationshipRepository.put(
                PartyRelationshipsDictionary.REPAIR.toString(),
                PartyRolesDictionary.INSURER.getRoleName(), insurer,
                PartyRolesDictionary.INSURED.getRoleName(), vehicle
        );
    }

    public void manufacturerWarrantyRegistered(PartyId distributorId, PartyId vehicleId) {
        Party distributor = partyRepository.put(distributorId.toUUID());
        Party vehicle = partyRepository.put(vehicleId.toUUID());

        partyRelationshipRepository.put(
                PartyRelationshipsDictionary.REPAIR.toString(),
                PartyRolesDictionary.GUARANTOR.getRoleName(), distributor,
                PartyRolesDictionary.CUSTOMER.getRoleName(), vehicle
        );
    }
}
