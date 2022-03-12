package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.entity.CarType;
import io.legacyfighter.cabs.entity.DriverSession;
import io.legacyfighter.cabs.repository.DriverRepository;
import io.legacyfighter.cabs.repository.DriverSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class DriverSessionService {

    private final DriverRepository driverRepository;

    private final DriverSessionRepository driverSessionRepository;

    private final CarTypeService carTypeService;

    private final Clock clock;

    public DriverSessionService(DriverRepository driverRepository, DriverSessionRepository driverSessionRepository, CarTypeService carTypeService, Clock clock) {
        this.driverRepository = driverRepository;
        this.driverSessionRepository = driverSessionRepository;
        this.carTypeService = carTypeService;
        this.clock = clock;
    }

    public DriverSession logIn(Long driverId, String plateNumber, CarType.CarClass carClass, String carBrand) {
        DriverSession session = new DriverSession();
        session.setDriver(driverRepository.getOne(driverId));
        session.setLoggedAt(Instant.now(clock));
        session.setCarClass(carClass);
        session.setPlatesNumber(plateNumber);
        session.setCarBrand(carBrand);
        carTypeService.registerActiveCar(session.getCarClass());
        return driverSessionRepository.save(session);
    }

    @Transactional
    public void logOut(Long sessionId) {
        DriverSession session = driverSessionRepository.getOne(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist");
        }
        carTypeService.unregisterActiveCar(session.getCarClass());
        session.setLoggedOutAt(Instant.now(clock));
    }

    @Transactional
    public void logOutCurrentSession(Long driverId) {
        DriverSession session = driverSessionRepository.findTopByDriverAndLoggedOutAtIsNullOrderByLoggedAtDesc(driverRepository.getOne(driverId));
        if (session != null) {
            session.setLoggedOutAt(Instant.now(clock));
            carTypeService.unregisterActiveCar(session.getCarClass());
        }
    }

    public List<DriverSession> findByDriver(Long driverId) {
        return driverSessionRepository.findByDriver(driverRepository.getOne(driverId));
    }
}
