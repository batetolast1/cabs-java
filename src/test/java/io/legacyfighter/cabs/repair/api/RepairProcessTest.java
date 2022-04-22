package io.legacyfighter.cabs.repair.api;

import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.repair.legacy.parts.Parts;
import io.legacyfighter.cabs.repair.model.roles.repair.RepairRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest
class RepairProcessTest {

    private final PartyId vehicle = new PartyId();

    private final PartyId handlingParty = new PartyId();

    @Autowired
    private RepairProcess vehicleRepairProcess;

    @Autowired
    private ContractManager contractManager;

    @Test
    void warrantyByInsuranceCoversAllButPaint() {
        // given
        contractManager.extendedWarrantyContractSigned(handlingParty, vehicle);
        // and
        Set<Parts> parts = Set.of(Parts.ENGINE, Parts.GEARBOX, Parts.PAINT, Parts.SUSPENSION);
        // and
        RepairRequest repairRequest = new RepairRequest(vehicle, parts);

        // when
        ResolveResult result = vehicleRepairProcess.resolveNonFunctionalStyle(repairRequest);

        // then
        new VehicleRepairAssert(result).by(handlingParty).free().allPartsBut(parts, Set.of(Parts.PAINT));
    }

    @Test
    void manufacturerWarrantyCoversAll() {
        // given
        contractManager.manufacturerWarrantyRegistered(handlingParty, vehicle);
        // and
        Set<Parts> parts = Set.of(Parts.ENGINE, Parts.GEARBOX, Parts.PAINT, Parts.SUSPENSION);
        // and
        RepairRequest repairRequest = new RepairRequest(vehicle, parts);

        // when
        ResolveResult result = vehicleRepairProcess.resolveNonFunctionalStyle(repairRequest);

        // then
        new VehicleRepairAssert(result).by(handlingParty).free().allParts(parts);
    }
}
