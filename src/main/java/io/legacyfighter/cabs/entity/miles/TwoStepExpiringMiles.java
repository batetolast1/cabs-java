package io.legacyfighter.cabs.entity.miles;

import java.time.Instant;
import java.util.Objects;

public class TwoStepExpiringMiles implements Miles {

    private Integer amount;

    private Instant whenFirstHalfExpires;

    private Instant whenExpires;

    public TwoStepExpiringMiles() {
        // for Jackson
    }

    TwoStepExpiringMiles(Integer milesAmount,
                         Instant whenFirstHalfExpires,
                         Instant whenExpires) {
        this.amount = milesAmount;
        this.whenFirstHalfExpires = whenFirstHalfExpires;
        this.whenExpires = whenExpires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TwoStepExpiringMiles that = (TwoStepExpiringMiles) o;
        return Objects.equals(amount, that.amount) && Objects.equals(whenFirstHalfExpires, that.whenFirstHalfExpires) && Objects.equals(whenExpires, that.whenExpires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, whenFirstHalfExpires, whenExpires);
    }

    @Override
    public String toString() {
        return "TwoStepExpiringMiles{" +
                "amount=" + amount +
                ", whenFirstHalfExpires=" + whenFirstHalfExpires +
                ", whenExpires=" + whenExpires +
                '}';
    }

    @Override
    public Integer getAmount(Instant at) {
        if (!this.whenFirstHalfExpires.isBefore(at)) {
            return this.amount;
        }
        if (!this.whenExpires.isBefore(at)) {
            return this.amount - halfOf(this.amount);
        }
        return 0;
    }

    @Override
    public Miles subtract(Integer milesAmount, Instant at) {
        if (milesAmount < 0) {
            throw new IllegalArgumentException("Incorrect amount of miles");
        }

        Integer currentAmount = this.getAmount(at);

        if (currentAmount < milesAmount) {
            throw new IllegalArgumentException("Insufficient amount of miles");
        }

        return new TwoStepExpiringMiles(currentAmount - milesAmount, this.whenFirstHalfExpires, this.whenExpires);
    }

    @Override
    public Instant expiresAt() {
        return this.whenExpires;
    }

    private Integer halfOf(Integer amount) {
        return amount / 2;
    }
}
