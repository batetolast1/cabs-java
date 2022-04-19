package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.repository.DriverPositionRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;

@Service
class PopulateTravelledDistanceService {

    private final DriverPositionRepository driverPositionRepository;

    private final TravelledDistanceService travelledDistanceService;

    private final Clock clock;

    public PopulateTravelledDistanceService(DriverPositionRepository driverPositionRepository,
                                            TravelledDistanceService travelledDistanceService,
                                            Clock clock) {
        this.driverPositionRepository = driverPositionRepository;
        this.travelledDistanceService = travelledDistanceService;
        this.clock = clock;
    }

    @Transactional
    void populate() {
        Instant now = Instant.now(clock);

        driverPositionRepository.findAllBySeenAtBefore(now)
                .forEach(driverPosition -> travelledDistanceService.addPosition(
                        driverPosition.getDriver().getId(),
                        driverPosition.getLatitude(),
                        driverPosition.getLongitude(),
                        driverPosition.getSeenAt()
                        )
                );
    }
}
