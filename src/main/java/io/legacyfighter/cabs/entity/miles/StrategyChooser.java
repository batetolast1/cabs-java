package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.entity.Client;

import java.time.DayOfWeek;
import java.util.Objects;

class StrategyChooser {

    private static final int CLAIM_COUNT_THRESHOLD = 3;
    private static final int TRANSIT_COUNT_THRESHOLD = 15;

    private static final TooManyClaimsRemoveStrategy TOO_MANY_CLAIMS_STRATEGY = new TooManyClaimsRemoveStrategy();
    private static final VipClientRemoveStrategy VIP_CLIENT_STRATEGY = new VipClientRemoveStrategy();
    private static final NormalClientRemoveStrategy NORMAL_CLIENT_STRATEGY = new NormalClientRemoveStrategy();
    private static final DefaultRemoveStrategy DEFAULT_STRATEGY = new DefaultRemoveStrategy();

    private final int transitCounter;
    private final int claimCounter;
    private final DayOfWeek dayOfWeek;

    StrategyChooser(int transitCounter,
                    int claimCounter,
                    DayOfWeek dayOfWeek) {
        this.transitCounter = transitCounter;
        this.claimCounter = claimCounter;
        this.dayOfWeek = dayOfWeek;
    }

    AwardedMilesRemoveStrategy chooseFor(Client client) {
        if (claimCounter >= CLAIM_COUNT_THRESHOLD) {
            return TOO_MANY_CLAIMS_STRATEGY;
        }
        if (Objects.equals(client.getType(), Client.Type.VIP)) {
            return VIP_CLIENT_STRATEGY;
        }
        if (transitCounter >= TRANSIT_COUNT_THRESHOLD && dayOfWeek == DayOfWeek.SUNDAY) {
            return VIP_CLIENT_STRATEGY;
        }
        if (transitCounter >= TRANSIT_COUNT_THRESHOLD) {
            return NORMAL_CLIENT_STRATEGY;
        }
        return DEFAULT_STRATEGY;
    }
}
