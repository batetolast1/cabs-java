package io.legacyfighter.cabs.entity.miles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantUntil.class, name = "ConstantUntil")
})
public interface Miles {

    Integer getAmountFor(Instant when);

    Miles subtract(Integer amount, Instant when);

    Instant expiresAt();
}
