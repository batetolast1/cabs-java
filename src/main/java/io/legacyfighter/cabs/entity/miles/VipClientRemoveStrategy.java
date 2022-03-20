package io.legacyfighter.cabs.entity.miles;

import org.springframework.util.comparator.Comparators;

import java.util.Comparator;

class VipClientRemoveStrategy implements AwardedMilesRemoveStrategy {

    private static final Comparator<AwardedMiles> SOON_TO_EXPIRE_FIRST_THEN_NON_EXPIRING =
            Comparator.comparing(AwardedMiles::cantExpire)
                    .thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow());

    @Override
    public int compare(AwardedMiles o1, AwardedMiles o2) {
        return SOON_TO_EXPIRE_FIRST_THEN_NON_EXPIRING.compare(o1, o2);
    }
}
