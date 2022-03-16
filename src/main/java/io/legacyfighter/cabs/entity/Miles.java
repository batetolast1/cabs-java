package io.legacyfighter.cabs.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConstantUntil.class, name = "ConstantUntil")
})
public interface Miles {

    Integer getAmountFor(Instant moment);

    Miles subtract(Integer amount, Instant moment);

    Instant expiresAt();
}
