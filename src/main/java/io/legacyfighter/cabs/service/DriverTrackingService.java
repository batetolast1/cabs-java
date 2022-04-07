package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.driverreport.travelleddistance.TravelledDistanceService;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.repository.DriverPositionRepository;
import io.legacyfighter.cabs.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DriverTrackingService {

    private final DriverPositionRepository driverPositionRepository;

    private final DriverRepository driverRepository;

    private final TravelledDistanceService travelledDistanceService;

    public DriverTrackingService(DriverPositionRepository driverPositionRepository,
                                 DriverRepository driverRepository,
                                 TravelledDistanceService travelledDistanceService) {
        this.driverPositionRepository = driverPositionRepository;
        this.driverRepository = driverRepository;
        this.travelledDistanceService = travelledDistanceService;
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

        DriverPosition position = driverPositionRepository.save(new DriverPosition(driver, seenAt, latitude, longitude));

        travelledDistanceService.addPosition(position);

        return position;
    }

    public Distance calculateTravelledDistance(Long driverId, Instant beginning, Instant end) {
        Driver driver = driverRepository.getOne(driverId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exists, id = " + driverId);
        }

        return travelledDistanceService.calculateDistance(driverId, beginning, end);
    }
}
