package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.carfleet.CarClass;
import io.legacyfighter.cabs.carfleet.CarTypeService;
import io.legacyfighter.cabs.common.EventsPublisher;
import io.legacyfighter.cabs.crm.Client;
import io.legacyfighter.cabs.crm.ClientRepository;
import io.legacyfighter.cabs.driverfleet.*;
import io.legacyfighter.cabs.dto.TransitDTO;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.entity.events.TransitCompleted;
import io.legacyfighter.cabs.geolocation.Distance;
import io.legacyfighter.cabs.geolocation.DistanceCalculator;
import io.legacyfighter.cabs.geolocation.GeocodingService;
import io.legacyfighter.cabs.geolocation.address.Address;
import io.legacyfighter.cabs.geolocation.address.AddressDTO;
import io.legacyfighter.cabs.geolocation.address.AddressRepository;
import io.legacyfighter.cabs.invocing.InvoiceGenerator;
import io.legacyfighter.cabs.loyalty.AwardsService;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.notification.DriverNotificationService;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.tracking.DriverPositionDTOV2;
import io.legacyfighter.cabs.tracking.DriverTrackingService;
import io.legacyfighter.cabs.transitdetails.TransitDetailsDTO;
import io.legacyfighter.cabs.transitdetails.TransitDetailsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// If this class will still be here in 2022 I will quit.
@Service
public class TransitService {
    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TransitRepository transitRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceGenerator invoiceGenerator;

    @Autowired
    private DriverNotificationService notificationService;

    @Autowired
    private DistanceCalculator distanceCalculator;

    @Autowired
    private CarTypeService carTypeService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DriverFeeService driverFeeService;

    @Autowired
    private Clock clock;

    @Autowired
    private AwardsService awardsService;

    @Autowired
    private TransitDetailsFacade transitDetailsFacade;

    @Autowired
    private EventsPublisher eventsPublisher;

    @Autowired
    private DriverTrackingService driverTrackingService;

    @Autowired
    private DriverService driverService;

    @Transactional
    public TransitDTO createTransit(TransitDTO transitDTO) {
        Address from = addressFromDto(transitDTO.getFrom());
        Address to = addressFromDto(transitDTO.getTo());
        return createTransit(transitDTO.getClientDTO().getId(), from, to, transitDTO.getCarClass());
    }

    private Address addressFromDto(AddressDTO addressDTO) {
        Address address = addressDTO.toAddressEntity();
        return addressRepository.save(address);

    }

    @Transactional
    public TransitDTO createTransit(Long clientId, Address from, Address to, CarClass carClass) {
        Client client = clientRepository.getOne(clientId);

        if (client == null) {
            throw new IllegalArgumentException("Client does not exist, id = " + clientId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(from);
        double[] geoTo = geocodingService.geocodeAddress(to);
        Distance km = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));
        Instant now = Instant.now(clock);
        Transit transit = new Transit(now, km);
        Money estimatedPrice = transit.estimateCost();
        transit = transitRepository.save(transit);
        transitDetailsFacade.transitRequested(now, transit.getId(), from, to, km, client, carClass, estimatedPrice, transit.getTariff());
        return loadTransit(transit.getId());
    }

    @Transactional
    public void changeTransitAddressFrom(Long transitId, Address newAddress) {
        newAddress = addressRepository.save(newAddress);
        Transit transit = transitRepository.getOne(transitId);
        TransitDetailsDTO transitDetails = findTransitDetails(transitId);
        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFromNew = geocodingService.geocodeAddress(newAddress);
        double[] geoFromOld = geocodingService.geocodeAddress(transitDetails.from.toAddressEntity());

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
        double distanceInKMeters = c * r;

        Distance newDistance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFromNew[0], geoFromNew[1], geoFromOld[0], geoFromOld[1]));
        transit.changePickupTo(newAddress, newDistance, distanceInKMeters);
        transitRepository.save(transit);
        transitDetailsFacade.pickupChangedTo(transit.getId(), newAddress, newDistance);

        for (Long driverId : transit.getProposedDrivers()) {
            notificationService.notifyAboutChangedTransitAddress(driverId, transitId);
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
        TransitDetailsDTO transitDetails = findTransitDetails(transitId);
        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(transitDetails.from.toAddressEntity());
        double[] geoTo = geocodingService.geocodeAddress(newAddress);

        Distance newDistance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));
        transit.changeDestinationTo(newAddress, newDistance);
        transitDetailsFacade.destinationChanged(transit.getId(), newAddress, newDistance);
        if (transit.getDriverId() != null) {
            notificationService.notifyAboutChangedTransitAddress(transit.getDriverId(), transitId);
        }
    }

    @Transactional
    public void cancelTransit(Long transitId) {
        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

        if (transit.getDriverId() != null) {
            notificationService.notifyAboutCancelledTransit(transit.getDriverId(), transitId);
        }

        transit.cancel();
        transitDetailsFacade.transitCancelled(transitId);
        transitRepository.save(transit);
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
        transitDetailsFacade.transitPublished(transitId, now);
        return findDriversForTransit(transitId);
    }

    // Abandon hope all ye who enter here...
    @Transactional
    public Transit findDriversForTransit(Long transitId) {
        Transit transit = transitRepository.getOne(transitId);
        TransitDetailsDTO transitDetails = findTransitDetails(transitId);

        if (transit != null) {
            if (transit.getStatus()
                    .equals(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT)) {


                Integer distanceToCheck = 0;

                // Tested on production, works as expected.
                // If you change this code and the system will collapse AGAIN, I'll find you...
                while (true) {
                    if (transit.getAwaitingDriversResponses()
                            > 4) {
                        return transit;
                    }

                    distanceToCheck++;

                    // FIXME: to refactor when the final business logic will be determined
                    if (transit.shouldNotWaitForDriverAnyMore(Instant.now(clock)) || distanceToCheck >= 20) {
                        transit.failDriverAssignment();
                        transitRepository.save(transit);
                        return transit;
                    }
                    double[] geocoded = new double[2];


                    try {
                        geocoded = geocodingService.geocodeAddress(addressRepository.getByHash(transitDetails.from.getHash()));
                    } catch (Exception e) {
                        // Geocoding failed! Ask Jessica or Bryan for some help if needed.
                    }

                    double longitude = geocoded[1];
                    double latitude = geocoded[0];

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
                    double latitudeMax = latitude + dLat *
                            180 / Math.PI;
                    double longitudeMin = longitude - dLon *
                            180 / Math.PI;
                    double longitudeMax = longitude + dLon * 180 / Math.PI;

                    List<CarClass> carClasses = choosePossibleCarClasses(transitDetails.carType);
                    if (carClasses.isEmpty()) {
                        return transit;
                    }

                    List<DriverPositionDTOV2> driversAvgPositions = driverTrackingService
                            .findActiveDriversNearby(latitudeMin, latitudeMax, longitudeMin, longitudeMax, latitude, longitude, carClasses);

                    if (driversAvgPositions.isEmpty()) {
                        //next iteration
                        continue;
                    }

                    // Iterate across average driver positions
                    for (DriverPositionDTOV2 driverAvgPosition : driversAvgPositions) {
                        if (transit.canProposeTo(driverAvgPosition.getDriverId())) {
                            transit.proposeTo(driverAvgPosition.getDriverId());
                            notificationService.notifyAboutPossibleTransit(driverAvgPosition.getDriverId(), transitId);
                        }

                    }
                    return transit;
                }
            } else {
                throw new IllegalStateException("..., id = " + transitId);
            }
        } else {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }

    }

    private List<CarClass> choosePossibleCarClasses(CarClass carClass) {
        List<CarClass> carClasses = new ArrayList<>();
        List<CarClass> activeCarClasses = carTypeService.findActiveCarClasses();
        if (carClass != null) {
            if (activeCarClasses.contains(carClass)) {
                carClasses.add(carClass);
            }
        } else {
            carClasses.addAll(activeCarClasses);
        }
        return carClasses;
    }

    @Transactional
    public void acceptTransit(Long driverId, Long transitId) {
        Driver driver = driverRepository.getOne(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        } else {
            Transit transit = transitRepository.getOne(transitId);

            if (transit == null) {
                throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
            } else {
                Instant now = Instant.now(clock);
                transit.acceptBy(driverId, now);
                driver.setOccupied(true);
                transitDetailsFacade.transitAccepted(transitId, now, driverId);
                transitRepository.save(transit);
                driverRepository.save(driver);
            }
        }
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
        transit.start(now);
        transitDetailsFacade.transitStarted(transitId, now);
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

        transit.rejectBy(driverId);
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
        TransitDetailsDTO transitDetails = findTransitDetails(transitId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver does not exist, id = " + driverId);
        }

        Transit transit = transitRepository.getOne(transitId);

        if (transit == null) {
            throw new IllegalArgumentException("Transit does not exist, id = " + transitId);
        }


        // FIXME later: add some exceptions handling
        double[] geoFrom = geocodingService.geocodeAddress(addressRepository.getByHash(transitDetails.from.getHash()));
        double[] geoTo = geocodingService.geocodeAddress(addressRepository.getByHash(transitDetails.to.getHash()));
        Distance distance = Distance.ofKm((float) distanceCalculator.calculateByMap(geoFrom[0], geoFrom[1], geoTo[0], geoTo[1]));
        Instant now = Instant.now(clock);
        transit.completeAt(now, destinationAddress, distance);
        Money driverFee = driverFeeService.calculateDriverFee(transit.getPrice(), driverId);
        driver.setOccupied(false);
        driverRepository.save(driver);
        awardsService.registerMiles(transitDetails.client.getId(), transitId);
        transitRepository.save(transit);
        transitDetailsFacade.transitCompleted(transitId, now, transit.getPrice(), driverFee);
        invoiceGenerator.generate(transit.getPrice().toInt(), transitDetails.client.getName() + " " + transitDetails.client.getLastName());
        eventsPublisher.publish(new TransitCompleted(
                transitDetails.client.getId(), transitId, transitDetails.from.getHash(), transitDetails.to.getHash(), transitDetails.started, now, Instant.now(clock))
        );
    }

    @Transactional
    public TransitDTO loadTransit(Long id) {
        TransitDetailsDTO transitDetails = findTransitDetails(id);
        Transit transit = transitRepository.getOne(id);
        Set<DriverDTO> proposedDrivers = driverService.loadDrivers(transit.getProposedDrivers());
        Set<DriverDTO> driverRejections = driverService.loadDrivers(transit.getDriverRejections());
        return new TransitDTO(transitDetails, proposedDrivers, driverRejections, transit.getDriverId());
    }

    private TransitDetailsDTO findTransitDetails(Long transitId) {
        return transitDetailsFacade.find(transitId);
    }
}
