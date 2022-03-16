package io.legacyfighter.cabs.entity;

import java.time.Instant;
import java.util.Objects;

public class ConstantUntil implements Miles {

    private final Integer amount;
    private final Instant whenExpires;

    private ConstantUntil(int amount, Instant whenExpires) {
        this.amount = amount;
        this.whenExpires = whenExpires;
    }

    public static ConstantUntil constantUntilForever(int amount) {
        return new ConstantUntil(amount, Instant.MAX);
    }

    public static ConstantUntil constantUntil(int amount, Instant whenExpires) {
        return new ConstantUntil(amount, whenExpires);
    }

    @Override
    public Integer getAmountFor(Instant moment) {
        return !whenExpires.isBefore(moment) ? amount : 0;
    }

    @Override
    public Miles subtract(Integer amount, Instant moment) {
        if (amount < 0) {
            throw new IllegalArgumentException("Incorrect amount of miles");
        }
        if (getAmountFor(moment) < amount) {
            throw new IllegalArgumentException("Insufficient amount of miles");
        }
        return new ConstantUntil(this.amount - amount, this.whenExpires);
    }

    @Override
    public Instant expiresAt() {
        return whenExpires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantUntil that = (ConstantUntil) o;
        return Objects.equals(amount, that.amount) && Objects.equals(whenExpires, that.whenExpires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, whenExpires);
    }
}
