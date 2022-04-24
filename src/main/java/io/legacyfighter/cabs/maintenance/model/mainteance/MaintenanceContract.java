package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaintenanceContract {

    private final Map<MaintenancePartsDictionary, Money> costForCoveredParts;

    private final Double paymentRatio;

    private MaintenanceContract(Map<MaintenancePartsDictionary, Money> costForCoveredParts,
                                double paymentRatio) {
        this.costForCoveredParts = costForCoveredParts;
        this.paymentRatio = paymentRatio;
    }

    public static MaintenanceContract freeMaintenanceContract() {
        Map<MaintenancePartsDictionary, Money> costForCoveredParts = Arrays.stream(MaintenancePartsDictionary.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        part -> Money.ZERO
                ));

        return new MaintenanceContract(costForCoveredParts, 100.0);
    }

    public static MaintenanceContract withFullPayment(Map<MaintenancePartsDictionary, Money> costForCoveredParts) {
        return new MaintenanceContract(costForCoveredParts, 100.0);
    }

    public static MaintenanceContract withPartialPayment(Map<MaintenancePartsDictionary, Money> costForCoveredParts,
                                                         double paymentRatio) {
        return new MaintenanceContract(costForCoveredParts, paymentRatio);
    }

    public Money getCostFor(MaintenancePartsDictionary part) {
        return this.costForCoveredParts.get(part);
    }

    public boolean isPartCovered(MaintenancePartsDictionary part) {
        return this.costForCoveredParts.containsKey(part);
    }

    public double getPaymentRatio() {
        return this.paymentRatio;
    }
}
