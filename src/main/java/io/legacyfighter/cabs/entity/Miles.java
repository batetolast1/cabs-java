package io.legacyfighter.cabs.entity;

import java.time.Instant;

public interface Miles {

    Integer getAmountFor(Instant moment);

    Miles subtract(Integer amount, Instant moment);

    Instant expiresAt();
}
