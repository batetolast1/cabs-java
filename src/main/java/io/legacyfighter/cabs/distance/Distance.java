package io.legacyfighter.cabs.distance;

import java.util.Locale;
import java.util.Objects;

public final class Distance {

    public static final Distance ZERO = Distance.ofKm(0);

    private static final double MILES_TO_KM_RATIO = 1.609344d;

    private final double km;

    private Distance(double km) {
        this.km = km;
    }

    public static Distance ofKm(float km) {
        return new Distance(km);
    }

    public static Distance ofKm(double km) {
        return new Distance(km);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Distance distance = (Distance) o;
        return Double.compare(distance.km, km) == 0;
    }

    @Override
    public String toString() {
        return "Distance{" +
                "km=" + km +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(km);
    }

    public float toKmInFloat() {
        return (float) this.km;
    }

    public double toKmInDouble() {
        return this.km;
    }

    public String printIn(String unit) {
        if (unit.equals("km")) {
            if (this.km == Math.ceil(this.km)) {
                return String.format(Locale.US, "%d", Math.round(this.km)) + "km";

            }
            return String.format(Locale.US, "%.3f", this.km) + "km";
        }

        if (unit.equals("miles")) {
            double miles = this.km / MILES_TO_KM_RATIO;
            if (miles == Math.ceil(miles)) {
                return String.format(Locale.US, "%d", Math.round(miles)) + "miles";
            }
            return String.format(Locale.US, "%.3f", miles) + "miles";

        }

        if (unit.equals("m")) {
            return String.format(Locale.US, "%d", Math.round(this.km * 1000)) + "m";
        }

        throw new IllegalArgumentException("Invalid unit " + unit);
    }
}
