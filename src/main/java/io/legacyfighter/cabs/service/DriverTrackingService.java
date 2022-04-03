package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.repository.DriverPositionRepository;
import io.legacyfighter.cabs.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverTrackingService {

    private final DriverPositionRepository positionRepository;

    private final DriverRepository driverRepository;

    private final DistanceCalculator distanceCalculator;

    public DriverTrackingService(DriverPositionRepository positionRepository,
                                 DriverRepository driverRepository,
                                 DistanceCalculator distanceCalculator) {
        this.positionRepository = positionRepository;
        this.driverRepository = driverRepository;
        this.distanceCalculator = distanceCalculator;
    }

    @Transactional
    public DriverPosition registerPosition(Long driverId,
                                           double latitude,
                                           double longitude,
                                           Instant seenAt) {
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exists, id = " + driverId);
        }

        if (!driver.getStatus().equals(Driver.Status.ACTIVE)) {
            throw new IllegalStateException("Driver is not active, cannot register position, id = " + driverId);
        }

        DriverPosition position = new DriverPosition();
        position.setDriver(driver);
        position.setSeenAt(seenAt);
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        return positionRepository.save(position);
    }

    public Distance calculateTravelledDistance(Long driverId, Instant from, Instant to) {
        Driver driver = driverRepository.getOne(driverId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exists, id = " + driverId);
        }

        List<DriverPosition> positions = positionRepository.findByDriverAndSeenAtBetweenOrderBySeenAtAsc(driver, from, to);
        double distanceTravelled = 0;

        if (positions.size() > 1) {
            DriverPosition previousPosition = positions.get(0);

            List<DriverPosition> driverPositions = positions.stream()
                    .skip(1)
                    .collect(Collectors.toList());

            for (DriverPosition position : driverPositions) {
                distanceTravelled += distanceCalculator.calculateByGeo(
                        previousPosition.getLatitude(),
                        previousPosition.getLongitude(),
                        position.getLatitude(),
                        position.getLongitude()
                );

                previousPosition = position;
            }
        }

        return Distance.ofKm(distanceTravelled);
    }
}
