package io.legacyfighter.cabs.repair.api;

import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.repair.legacy.parts.Parts;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleRepairAssert {

    private final ResolveResult result;

    public VehicleRepairAssert(ResolveResult result) {
        this(result, true);
    }

    public VehicleRepairAssert(ResolveResult result, boolean demandSuccess) {
        this.result = result;

        if (demandSuccess) {
            assertThat(result.getStatus()).isEqualTo(ResolveResult.Status.SUCCESS);
        } else {
            assertThat(result.getStatus()).isEqualTo(ResolveResult.Status.ERROR);
        }
    }

    public VehicleRepairAssert free() {
        assertThat(result.getTotalCost()).isEqualTo(Money.ZERO);

        return this;
    }

    public VehicleRepairAssert allParts(Set<Parts> parts) {
        assertThat(result.getAcceptedParts()).containsExactlyInAnyOrderElementsOf(parts);

        return this;
    }

    public VehicleRepairAssert by(PartyId handlingParty) {
        assertThat(result.getHandlingParty()).isEqualTo(handlingParty.toUUID());

        return this;
    }

    public VehicleRepairAssert allPartsBut(Set<Parts> parts, Set<Parts> excludedParts) {
        Set<Parts> expectedParts = new HashSet<>(parts);
        expectedParts.removeAll(excludedParts);

        assertThat(result.getAcceptedParts()).isEqualTo(expectedParts);

        return this;
    }
}
