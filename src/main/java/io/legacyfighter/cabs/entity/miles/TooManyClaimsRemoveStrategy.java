package io.legacyfighter.cabs.entity.miles;

import org.springframework.util.comparator.Comparators;

import java.util.Comparator;

class TooManyClaimsRemoveStrategy implements AwardedMilesRemoveStrategy {

    private static final Comparator<AwardedMiles> FIRST_NON_EXPIRING_THEN_LATEST_TO_EXPIRE =
            Comparator.comparing(AwardedMiles::getExpirationDate, Comparators.nullsHigh())
                    .reversed()
                    .thenComparing(Comparators.nullsHigh());

    @Override
    public int compare(AwardedMiles o1, AwardedMiles o2) {
        return FIRST_NON_EXPIRING_THEN_LATEST_TO_EXPIRE.compare(o1, o2);
    }
}
