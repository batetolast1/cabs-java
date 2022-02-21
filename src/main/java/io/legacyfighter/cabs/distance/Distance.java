package io.legacyfighter.cabs.distance;

import java.util.Locale;
import java.util.Objects;

public final class Distance {

    public static final Distance ZERO = Distance.ofKm(0);

    private static final float MILES_TO_KM_RATIO = 1.609344f;

    private final float km;

    private Distance(float km) {
        this.km = km;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Distance distance = (Distance) o;
        return Float.compare(distance.km, km) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(km);
    }

    public static Distance ofKm(float km) {
        return new Distance(km);
    }

    public float toKmInFloat() {
        return km;
    }

    public String printIn(String unit) {
        if (unit.equals("km")) {
            if (km == Math.ceil(km)) {
                return String.format(Locale.US, "%d", Math.round(km)) + "km";

            }
            return String.format(Locale.US, "%.3f", km) + "km";
        }
        if (unit.equals("miles")) {
            float km = this.km / MILES_TO_KM_RATIO;
            if (km == Math.ceil(km)) {
                return String.format(Locale.US, "%d", Math.round(km)) + "miles";
            }
            return String.format(Locale.US, "%.3f", km) + "miles";

        }
        if (unit.equals("m")) {
            return String.format(Locale.US, "%d", Math.round(km * 1000)) + "m";
        }
        throw new IllegalArgumentException("Invalid unit " + unit);
    }
}
