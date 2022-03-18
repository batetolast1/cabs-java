package io.legacyfighter.cabs.entity.miles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantUntil.class, name = "Expiring"),
        @JsonSubTypes.Type(value = TwoStepExpiringMiles.class, name = "TwoStep")
})
public interface Miles {

    Integer getAmount(Instant at);

    Miles subtract(Integer milesAmount, Instant at);

    Instant expiresAt();
}
