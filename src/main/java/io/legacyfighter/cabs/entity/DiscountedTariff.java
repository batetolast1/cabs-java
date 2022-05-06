package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;

public class DiscountedTariff implements Tariff {

    private Tariff tariff;

    private String name;

    private int percentage;

    public DiscountedTariff() {
        // for Jackson
    }

    private DiscountedTariff(Tariff tariff, String name, int percentage) {
        this.tariff = tariff;
        this.name = name;
        this.percentage = percentage;
    }

    public static DiscountedTariff discounted(Tariff tariff, String name, int percentage) {
        return new DiscountedTariff(tariff, name, percentage);
    }

    @Override
    public Money calculateCost(Distance distance) {
        return this.tariff.calculateCost(distance).percentage(this.percentage);
    }

    @Override
    public Float getKmRate() {
        return this.tariff.getKmRate() * this.percentage / 100;
    }

    @Override
    public String getName() {
        return this.tariff.getName() + " " + this.name;
    }

    @Override
    public Integer getBaseFee() {
        return this.tariff.getBaseFee() * this.percentage / 100;
    }
}
