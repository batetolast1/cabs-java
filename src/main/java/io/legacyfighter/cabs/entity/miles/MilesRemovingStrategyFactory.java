package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.repository.ClaimRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;

@Service
public class MilesRemovingStrategyFactory {

    private final TransitRepository transitRepository;
    private final ClaimRepository claimRepository;
    private final Clock clock;

    public MilesRemovingStrategyFactory(TransitRepository transitRepository,
                                        ClaimRepository claimRepository,
                                        Clock clock) {
        this.transitRepository = transitRepository;
        this.claimRepository = claimRepository;
        this.clock = clock;
    }

    public AwardedMilesRemoveStrategy chooseFor(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        int transitCounter = transitRepository.countByClient(client);
        int claimCounter = claimRepository.countByOwner(client);
        DayOfWeek dayOfWeek = currentDay();

        return new StrategyChooser(transitCounter, claimCounter, dayOfWeek).chooseFor(client);
    }

    private DayOfWeek currentDay() {
        return Instant.now(clock)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getDayOfWeek();
    }
}
