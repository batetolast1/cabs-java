package io.legacyfighter.cabs.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.money.Money;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultTariff.class, name = "DefaultTariff"),
        @JsonSubTypes.Type(value = DiscountedTariff.class, name = "Discounted")
})
public interface Tariff {

    Money calculateCost(Distance distance);

    Float getKmRate();

    String getName();

    Integer getBaseFee();
}
