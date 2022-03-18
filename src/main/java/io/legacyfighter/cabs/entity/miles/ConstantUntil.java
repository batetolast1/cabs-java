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

    public static ConstantUntil constantUntilForever(int milesAmount) {
        return new ConstantUntil(milesAmount, Instant.MAX);
    }

    public static ConstantUntil constantUntil(int milesAmount, Instant expireAt) {
        return new ConstantUntil(milesAmount, expireAt);
    }

    @Override
    public Integer getAmount(Instant at) {
        return !this.whenExpires.isBefore(at) ? this.amount : 0;
    }

    @Override
    public Miles subtract(Integer milesAmount, Instant at) {//20
        if (milesAmount < 0) {
            throw new IllegalArgumentException("Incorrect amount of miles");
        }

        Integer currentAmount = this.getAmount(at); // 50

        if (currentAmount < milesAmount) {
            throw new IllegalArgumentException("Insufficient amount of miles");
        }

        return new ConstantUntil(this.amount - milesAmount, this.whenExpires);
    }

    @Override
    public Instant expiresAt() {
        return this.whenExpires;
    }
}
