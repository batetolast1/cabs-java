package io.legacyfighter.cabs.entity.miles;

import java.time.Instant;
import java.util.Objects;

public class ConstantUntil implements Miles {

    private Integer amount;

    private Instant whenExpires;

    public ConstantUntil() {
        // for Jackson
    }

    private ConstantUntil(int amount, Instant whenExpires) {
        this.amount = amount;
        this.whenExpires = whenExpires;
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

    @Override
    public String toString() {
        return "ConstantUntil{" +
                "amount=" + amount +
                ", whenExpires=" + whenExpires +
                '}';
    }

    public static ConstantUntil constantUntilForever(int amount) {
        return new ConstantUntil(amount, Instant.MAX);
    }

    public static ConstantUntil constantUntil(int amount, Instant whenExpires) {
        return new ConstantUntil(amount, whenExpires);
    }

    @Override
    public Integer getAmountFor(Instant when) {
        return !this.whenExpires.isBefore(when) ? this.amount : 0;
    }

    @Override
    public Miles subtract(Integer amount, Instant when) {
        if (amount < 0) {
            throw new IllegalArgumentException("Incorrect amount of miles");
        }

        Integer currentAmount = this.getAmountFor(when);

        if (currentAmount < amount) {
            throw new IllegalArgumentException("Insufficient amount of miles");
        }

        return new ConstantUntil(this.amount - amount, this.whenExpires);
    }

    @Override
    public Instant expiresAt() {
        return this.whenExpires;
    }
}
