package io.legacyfighter.cabs.maintenance.model.mainteance;

import io.legacyfighter.cabs.maintenance.model.dict.MaintenancePartsDictionary;
import io.legacyfighter.cabs.money.Money;

import java.util.Set;

public class ContractBasedCostStrategy implements CalculateMaintenanceCostStrategy {

    @Override
    public Money calculateCost(Set<MaintenancePartsDictionary> parts,
                               MaintenanceContract contract) {
        return parts.stream()
                .map(contract::getCostFor)
                .reduce(Money.ZERO, Money::add)
                .percentage(contract.getPaymentRatio());
    }
}
