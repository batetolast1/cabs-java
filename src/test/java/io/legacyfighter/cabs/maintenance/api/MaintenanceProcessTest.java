package io.legacyfighter.cabs.maintenance.api;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.MaintenanceContract;
import io.legacyfighter.cabs.maintenance.model.roles.mainteance.MaintenanceRequest;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.party.api.PartyId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MaintenanceProcessTest {

    private final PartyId handlingParty = new PartyId();

    private final PartyId vehicle = new PartyId();

    @Autowired
    private MaintenanceContractManager maintenanceContractManager;

    @Autowired
    private MaintenanceProcess maintenanceProcess;

    @Test
    void asoMaintenanceCoversAllFromContractForFree() {
        // given
        maintenanceContractManager.asoMaintenanceRegistered(handlingParty, vehicle);
        // and
        Set<MaintenancePartsDictionary> maintenanceParts = Set.of(ENGINE, SUSPENSION, WINDOWS);
        // and
        MaintenanceContract maintenanceContract = new MaintenanceContract(
                Map.of(
                        ENGINE, Money.ZERO,
                        SUSPENSION, Money.ZERO
                )
        );
        // and
        MaintenanceRequest maintenanceRequest = new MaintenanceRequest(vehicle, maintenanceParts, maintenanceContract);

        // when
        MaintenanceResolveResult result = maintenanceProcess.resolve(maintenanceRequest);

        // then
        new MaintenanceRepairAssert(result)
                .by(handlingParty)
                .free()
                .allPartsBut(maintenanceParts, Set.of(WINDOWS));
    }

    @Test
    void maintenanceByThirdPartyCoversAllFromContractForAgreedPrice() {
        // given
        maintenanceContractManager.thirdPartyMaintenanceContractSigned(handlingParty, vehicle);
        // and
        Set<MaintenancePartsDictionary> maintenanceParts = Set.of(ENGINE, SUSPENSION, WINDOWS);
        // and
        MaintenanceContract maintenanceContract = new MaintenanceContract(
                Map.of(
                        ENGINE, new Money(1000),
                        SUSPENSION, new Money(2000),
                        PAINT, new Money(500)
                )
        );
        // and
        MaintenanceRequest maintenanceRequest = new MaintenanceRequest(vehicle, maintenanceParts, maintenanceContract);

        // when
        MaintenanceResolveResult result = maintenanceProcess.resolve(maintenanceRequest);

        // then
        new MaintenanceRepairAssert(result)
                .by(handlingParty)
                .totalCost(new Money(3000))
                .allPartsBut(maintenanceParts, Set.of(WINDOWS));
    }

    @Test
    void selfServiceMaintenanceCoversAllFromContractForAgreedPriceAndCoverageRatio() {
        // given
        maintenanceContractManager.selfServiceMaintenanceContractSigned(handlingParty, vehicle);
        // and
        Set<MaintenancePartsDictionary> maintenanceParts = Set.of(ENGINE, WINDSHIELD_WASHER_FLUID, WHEELS, WINDOWS);
        // and
        MaintenanceContract maintenanceContract = new MaintenanceContract(
                Map.of(
                        WINDSHIELD_WASHER_FLUID, new Money(500),
                        WHEELS, new Money(750),
                        PAINT, new Money(250)
                ),
                70
        );
        // and
        MaintenanceRequest maintenanceRequest = new MaintenanceRequest(vehicle, maintenanceParts, maintenanceContract);

        // when
        MaintenanceResolveResult result = maintenanceProcess.resolve(maintenanceRequest);

        // then
        new MaintenanceRepairAssert(result)
                .by(handlingParty)
                .totalCost(new Money(875))
                .allPartsBut(maintenanceParts, Set.of(ENGINE, WINDOWS));
    }

    private static class MaintenanceRepairAssert {

        private final MaintenanceResolveResult result;

        public MaintenanceRepairAssert(MaintenanceResolveResult result) {
            this(result, true);
        }

        public MaintenanceRepairAssert(MaintenanceResolveResult result, boolean demandSuccess) {
            this.result = result;

            if (demandSuccess) {
                assertThat(result.getStatus()).isEqualTo(MaintenanceResolveResult.Status.SUCCESS);
            } else {
                assertThat(result.getStatus()).isEqualTo(MaintenanceResolveResult.Status.ERROR);
            }
        }

        public MaintenanceRepairAssert free() {
            assertThat(this.result.getTotalCost()).isEqualTo(Money.ZERO);

            return this;
        }

        public MaintenanceRepairAssert totalCost(Money cost) {
            assertThat(this.result.getTotalCost()).isEqualTo(cost);

            return this;
        }

        public MaintenanceRepairAssert allParts(Set<MaintenancePartsDictionary> parts) {
            assertThat(this.result.getAcceptedParts()).containsExactlyInAnyOrderElementsOf(parts);

            return this;
        }

        public MaintenanceRepairAssert by(PartyId handlingParty) {
            assertThat(this.result.getHandlingParty()).isEqualTo(handlingParty.toUUID());

            return this;
        }

        public MaintenanceRepairAssert allPartsBut(Set<MaintenancePartsDictionary> parts, Set<MaintenancePartsDictionary> excludedParts) {
            Set<MaintenancePartsDictionary> expectedParts = new HashSet<>(parts);
            expectedParts.removeAll(excludedParts);

            assertThat(this.result.getAcceptedParts()).isEqualTo(expectedParts);

            return this;
        }
    }
}
