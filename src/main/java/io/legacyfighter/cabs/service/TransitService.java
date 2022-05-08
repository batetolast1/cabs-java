package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.dto.AddressDTO;
import io.legacyfighter.cabs.dto.DriverPositionDTOV2;
import io.legacyfighter.cabs.dto.TransitDTO;
import io.legacyfighter.cabs.entity.*;
import io.legacyfighter.cabs.entity.events.TransitCompleted;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

// If this class will still be here in 2022 I will quit.
@Service
public class TransitService {

    private final DriverRepository driverRepository;

    private final TransitRepository transitRepository;

    private final ClientRepository clientRepository;

    private final InvoiceGenerator invoiceGenerator;

    private final DriverNotificationService notificationService;

    private final DistanceCalculator distanceCalculator;

    private final DriverPositionRepository driverPositionRepository;

    private final DriverSessionRepository driverSessionRepository;

    private final CarTypeService carTypeService;

    private final GeocodingService geocodingService;

    private final AddressRepository addressRepository;

    private final DriverFeeService driverFeeService;

    private final Clock clock;

    private final AwardsService awardsService;

    private final ApplicationEventPublisher eventPublisher;

    public TransitService(GeocodingService geocodingService,
                          DriverRepository driverRepository,
                          TransitRepository transitRepository,
                          ClientRepository clientRepository,
                          InvoiceGenerator invoiceGenerator,
                          DriverNotificationService notificationService,
                          DistanceCalculator distanceCalculator,
                          Clock clock,
                          DriverPositionRepository driverPositionRepository,
                          AwardsService awardsService,
                          DriverSessionRepository driverSessionRepository,
                          DriverFeeService driverFeeService,
                          CarTypeService carTypeService,
                          AddressRepository addressRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.geocodingService = geocodingService;
        this.driverRepository = driverRepository;
        this.transitRepository = transitRepository;
        this.clientRepository = clientRepository;
        this.invoiceGenerator = invoiceGenerator;
        this.notificationService = notificationService;
        this.distanceCalculator = distanceCalculator;
        this.clock = clock;
        this.driverPositionRepository = driverPositionRepository;
        this.awardsService = awardsService;
        this.driverSessionRepository = driverSessionRepository;
        this.driverFeeService = driverFeeService;
        this.carTypeService = carTypeService;
        this.addressRepository = addressRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Transit createTransit(TransitDTO transitDTO) {
        Address from = addressFromDto(transitDTO.getFrom());
        Address to = addressFromDto(transitDTO.getTo());
        return createTransit(transitDTO.getClientDTO().getId(), from, to, transitDTO.getCarClass());
    }

    private Address addressFromDto(AddressDTO addressDTO) {
        Address address = addressDTO.toAddressEntity();
        return addressRepository.save(address);

    }

    @Transactional
    public Transit createTransit(Long clientId, Address from, Address to, CarType.CarClass carClass) {
        Client client = clientRepository.getOne(clientId);

        if (client == null) {
            throw new IllegalArgumentException("Client does not exist, id = " + clientId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(from);
        double[] geoTo = geocodingService.geocodeAddress(to);
        Distance km = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));
        Instant now = Instant.now(clock);

        Transit transit = new Transit(from, to, client, carClass, now, km);
        return transitRepository.save(transit);
    }

    @Transactional
    public void changeTransitAddressFrom(Long transitId, Address newAddress) {
        newAddress = addressRepository.save(newAddress);
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFromNew = geocodingService.geocodeAddress(newAddress);
        double[] geoFromOld = geocodingService.geocodeAddress(transit.getFrom());

        // https://www.geeksforgeeks.org/program-distance-two-points-earth/
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        double lon1 = Math.toRadians(geoFromNew[1]);
        double lon2 = Math.toRadians(geoFromOld[1]);
        double lat1 = Math.toRadians(geoFromNew[0]);
        double lat2 = Math.toRadians(geoFromOld[0]);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956 for miles
        double r = 6371;

        // calculate the result
        double distanceFromPreviousPickup = c * r;
        Distance newDistance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFromNew[0], geoFromNew[1], geoFromOld[0], geoFromOld[1]));

        transit.changePickupTo(newAddress, newDistance, distanceFromPreviousPickup);
        transitRepository.save(transit);

        for (Driver driver : transit.getProposedDrivers()) {
            notificationService.notifyAboutChangedTransitAddress(driver.getId(), transitId);
        }
    }

    @Transactional
    public void changeTransitAddressTo(Long transitId, AddressDTO newAddress) {
        changeTransitAddressTo(transitId, newAddress.toAddressEntity());
    }

    @Transactional
    public void changeTransitAddressFrom(Long transitId, AddressDTO newAddress) {
        changeTransitAddressFrom(transitId, newAddress.toAddressEntity());
    }

    @Transactional
    public void changeTransitAddressTo(Long transitId, Address newAddress) {
        addressRepository.save(newAddress);
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(transit.getFrom());
        double[] geoTo = geocodingService.geocodeAddress(newAddress);
        Distance newDistance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));

        Rule rule = new OrRule(Set.of(
                new StatusRule(Transit.Status.DRAFT),
                new StatusRule(Transit.Status.CANCELLED),
                new StatusRule(Transit.Status.DRIVER_ASSIGNMENT_FAILED),
                new NoFurtherThanRule(Distance.ofKm(50), Transit.Status.TRANSIT_TO_PASSENGER),
                new NoFurtherThanRule(Distance.ofKm(45), Transit.Status.IN_TRANSIT))
        );

        transit.changeDestinationTo(newAddress, newDistance, rule);

        if (transit.getDriver() != null) {
            notificationService.notifyAboutChangedTransitAddress(transit.getDriver().getId(), transitId);
        }
    }

    @Transactional
    public void cancelTransit(Long transitId) {
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        if (transit.canCancel()) {
            if (transit.getDriver() != null) {
                notificationService.notifyAboutCancelledTransit(transit.getDriver().getId(), transitId);
            }

            transit.cancel();
            transitRepository.save(transit);
        }
    }

    @Transactional
    public Transit publishTransit(Long transitId) {
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        Instant now = Instant.now(clock);

        transit.publishAt(now);
        transitRepository.save(transit);

        return findDriversForTransit(transitId);
    }

    // Abandon hope all ye who enter here...
    @Transactional
    public Transit findDriversForTransit(Long transitId) {
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }
        if (!transit.getStatus().equals(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT)) {
            throw new IllegalStateException("..., id = " + transitId);
        }

        int distanceToCheck = 0;

        // Tested on production, works as expected.
        // If you change this code and the system will collapse AGAIN, I'll find you...
        while (true) {
            if (transit.getAwaitingDriversResponses() > 4) {
                return transit;
            }

            distanceToCheck++;

            // FIXME: to refactor when the final business logic will be determined
            Instant now = Instant.now(clock);
            if (transit.shouldNotWaitForDriverAnyMore(now) || (distanceToCheck >= 20)) {
                transit.failDriverAssignment();
                transitRepository.save(transit);
                return transit;
            }

            double[] geocoded = new double[2];
            try {
                geocoded = geocodingService.geocodeAddress(transit.getFrom());
            } catch (Exception e) {
                // Geocoding failed! Ask Jessica or Bryan for some help if needed.
            }

            double longitude = geocoded[1];
            double latitude = geocoded[0];

            List<DriverPositionDTOV2> driversAvgPositions = findDriverPositionsInArea(distanceToCheck, longitude, latitude, 5);
            if (!driversAvgPositions.isEmpty()) {
                List<CarType.CarClass> availableCarClasses = getAvailableCarClasses(transit);
                if (availableCarClasses.isEmpty()) {
                    return transit;
                }

                driversAvgPositions = findNearbyAvailableDrivers(longitude, latitude, driversAvgPositions, availableCarClasses, 20);

                // Iterate across average driver positions
                for (DriverPositionDTOV2 driverAvgPosition : driversAvgPositions) {
                    Driver driver = driverAvgPosition.getDriver();
                    if (canProposeToDriver(transit, driver)) {
                        transit.proposeTo(driver);
                        notificationService.notifyAboutPossibleTransit(driver.getId(), transitId);
                    }
                }

                transitRepository.save(transit);
            } else {
                // Next iteration, no drivers at specified area
                continue;
            }
        }
    }

    private boolean canProposeToDriver(Transit transit, Driver driver) {
        return driver.getStatus().equals(Driver.Status.ACTIVE) && !driver.isOccupied()
                && transit.canProposeTo(driver);
    }

    private List<CarType.CarClass> getAvailableCarClasses(Transit transit) {
        List<CarType.CarClass> activeCarClasses = carTypeService.findActiveCarClasses();
        if (activeCarClasses.isEmpty() ||
                (transit.getCarType() != null && !activeCarClasses.contains(transit.getCarType()))) {
            return Collections.emptyList();
        }

        List<CarType.CarClass> carClasses = new ArrayList<>();
        if (transit.getCarType() != null && activeCarClasses.contains(transit.getCarType())) {
            carClasses.add(transit.getCarType());
        } else {
            carClasses.addAll(activeCarClasses);
        }
        return carClasses;
    }

    private List<DriverPositionDTOV2> findNearbyAvailableDrivers(double longitude,
                                                                 double latitude,
                                                                 List<DriverPositionDTOV2> driversAvgPositions,
                                                                 List<CarType.CarClass> carClasses,
                                                                 int maxSize) {
        Comparator<DriverPositionDTOV2> comparator = Comparator.comparingDouble(
                (DriverPositionDTOV2 d) ->
                        Math.sqrt(Math.pow(latitude - d.getLatitude(), 2) + Math.pow(longitude - d.getLongitude(), 2))
        );

        driversAvgPositions = driversAvgPositions.stream()
                .sorted(comparator)
                .limit(maxSize)
                .collect(toList());

        List<Driver> drivers = driversAvgPositions.stream()
                .map(DriverPositionDTOV2::getDriver)
                .collect(toList());

        List<Long> activeDriverIdsInSpecificCar = driverSessionRepository.findAllByLoggedOutAtNullAndDriverInAndCarClassIn(drivers, carClasses)
                .stream()
                .map(ds -> ds.getDriver().getId()).collect(toList());

        return driversAvgPositions
                .stream()
                .filter(dp -> activeDriverIdsInSpecificCar.contains(dp.getDriver().getId()))
                .collect(toList());
    }

    private List<DriverPositionDTOV2> findDriverPositionsInArea(Integer distanceToCheck,
                                                                double longitude,
                                                                double latitude,
                                                                int minutes) {
        //https://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
        //Earth’s radius, sphere
        //double R = 6378;
        double R = 6371; // Changed to 6371 due to Copy&Paste pattern from different source

        //offsets in meters
        double dn = distanceToCheck;
        double de = distanceToCheck;

        //Coordinate offsets in radians
        double dLat = dn / R;
        double dLon = de / (R * Math.cos(Math.PI * latitude / 180));

        //Offset positions, decimal degrees
        double latitudeMin = latitude - dLat * 180 / Math.PI;
        double latitudeMax = latitude + dLat * 180 / Math.PI;
        double longitudeMin = longitude - dLon * 180 / Math.PI;
        double longitudeMax = longitude + dLon * 180 / Math.PI;

        return driverPositionRepository
                .findAverageDriverPositionSince(
                        latitudeMin,
                        latitudeMax,
                        longitudeMin,
                        longitudeMax,
                        Instant.now(clock).minus(minutes, ChronoUnit.MINUTES)
                );
    }

    @Transactional
    public void acceptTransit(Long driverId, Long transitId) {
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        }

        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        Instant now = Instant.now(clock);

        transit.acceptBy(driver, now);
        transitRepository.save(transit);

        driver.setOccupied(true);
        driverRepository.save(driver);
    }

    @Transactional
    public void startTransit(Long driverId, Long transitId) {
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        }

        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        Instant now = Instant.now(clock);
        transit.startAt(now);

        transitRepository.save(transit);
    }

    @Transactional
    public void rejectTransit(Long driverId, Long transitId) {
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        }

        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        transit.rejectBy(driver);
        transitRepository.save(transit);
    }

    @Transactional
    public void completeTransit(Long driverId, Long transitId, AddressDTO destinationAddress) {
        completeTransit(driverId, transitId, destinationAddress.toAddressEntity());
    }

    @Transactional
    public void completeTransit(Long driverId, Long transitId, Address destinationAddress) {
        destinationAddress = addressRepository.save(destinationAddress);
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        }

        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(transit.getFrom());
        double[] geoTo = geocodingService.geocodeAddress(transit.getTo());

        Distance distance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));
        Instant now = Instant.now(clock);

        transit.completeAt(now, destinationAddress, distance);

        driver.setOccupied(false);
        Money driverFee = driverFeeService.calculateDriverFee(transitId);
        transit.setDriversFee(driverFee);
        driverRepository.save(driver);

        awardsService.registerMiles(transit.getClient().getId(), transitId);

        transitRepository.save(transit);

        invoiceGenerator.generate(transit.getPrice().toInt(), transit.getClient().getName() + " " + transit.getClient().getLastName());

        eventPublisher.publishEvent(
                new TransitCompleted(
                        transit.getClient().getId(),
                        transitId,
                        transit.getFrom().getHash(),
                        transit.getTo().getHash(),
                        transit.getStarted(),
                        transit.getCompleteAt(),
                        Instant.now(clock)
                )
        );
    }

    @Transactional
    public TransitDTO loadTransit(Long id) {
        return new TransitDTO(transitRepository.getOne(id));
    }
}
