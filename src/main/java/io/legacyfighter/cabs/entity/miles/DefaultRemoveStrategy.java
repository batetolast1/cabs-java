package io.legacyfighter.cabs.entity.miles;

import java.util.Comparator;

class DefaultRemoveStrategy implements AwardedMilesRemoveStrategy {

    private static final Comparator<AwardedMiles> OLDEST_FIRST = Comparator.comparing(AwardedMiles::getDate);

    @Override
    public int compare(AwardedMiles o1, AwardedMiles o2) {
        return OLDEST_FIRST.compare(o1, o2);
    }
}
