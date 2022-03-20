package io.legacyfighter.cabs.entity.miles;

import java.util.Comparator;

class NormalClientRemoveStrategy implements AwardedMilesRemoveStrategy {

    private static final Comparator<AwardedMiles> LATEST_TO_EXPIRE_FIRST_THEN_NON_EXPIRING =
            Comparator.comparing(AwardedMiles::cantExpire)
                    .thenComparing(AwardedMiles::getDate);

    @Override
    public int compare(AwardedMiles o1, AwardedMiles o2) {
        return LATEST_TO_EXPIRE_FIRST_THEN_NON_EXPIRING.compare(o1, o2);
    }
}
