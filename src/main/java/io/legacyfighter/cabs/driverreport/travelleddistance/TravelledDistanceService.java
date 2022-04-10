package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.repository.DriverPositionRepository;
import io.legacyfighter.cabs.service.DistanceCalculator;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class TravelledDistanceService {

    private final TravelledDistanceRepository travelledDistanceRepository;

    private final DriverPositionRepository driverPositionRepository;

    private final Clock clock;

    private final DistanceCalculator distanceCalculator;

    public TravelledDistanceService(TravelledDistanceRepository travelledDistanceRepository,
                                    DriverPositionRepository driverPositionRepository,
                                    Clock clock,
                                    DistanceCalculator distanceCalculator) {
        this.travelledDistanceRepository = travelledDistanceRepository;
        this.driverPositionRepository = driverPositionRepository;
        this.clock = clock;
        this.distanceCalculator = distanceCalculator;
    }

    @Transactional
    public void addPosition(Long driverId,
                            double latitude,
                            double longitude,
                            Instant seenAt) {
        Instant now = Instant.now(clock);

        if (seenAt.isAfter(now)) {
            throw new IllegalArgumentException();
        }

        TimeSlot seenAtTimeSlot = TimeSlot.timeSlotThatContains(seenAt);

        TravelledDistance seenAtTravelledDistance = travelledDistanceRepository.findTravelledDistanceByTimeSlotAndDriverId(seenAtTimeSlot, driverId);

        if (seenAtTravelledDistance != null) {
            if (seenAt.isAfter(seenAtTravelledDistance.getLastPositionTime())) {
                addDistance(latitude, longitude, seenAt, seenAtTravelledDistance);
            } else {
                recalculateDistanceFor(seenAtTravelledDistance, seenAtTimeSlot, driverId);
            }
        } else {
            TimeSlot currentTimeSlot = TimeSlot.timeSlotThatContains(now);

            if (currentTimeSlot.contains(seenAt)) {
                createTravelledDistance(driverId, currentTimeSlot, latitude, longitude, seenAt);

                TimeSlot previousTimeSlot = currentTimeSlot.previous();
                if (previousTimeSlot.endsAt(seenAt)) {
                    TravelledDistance previousTravelledDistance = travelledDistanceRepository.findTravelledDistanceByTimeSlotAndDriverId(previousTimeSlot, driverId);

                    if (previousTravelledDistance != null) {
                        addDistance(latitude, longitude, seenAt, previousTravelledDistance);
                    }
                }
            } else {
                createTravelledDistance(driverId, seenAtTimeSlot, latitude, longitude, seenAt);
            }
        }
    }

    public Distance calculateDistance(Long driverId,
                                      Instant beginning,
                                      Instant end) {
        TimeSlot leftMostTimeSlot = TimeSlot.timeSlotThatContains(beginning);
        TimeSlot rightMostTimeSlot = TimeSlot.timeSlotThatContains(end);

        double km = travelledDistanceRepository.calculateDistance(leftMostTimeSlot.getBeginning(), rightMostTimeSlot.getEnd(), driverId);

        return Distance.ofKm(km);
    }

    private void addDistance(double latitude,
                             double longitude,
                             Instant lastPositionTime,
                             TravelledDistance travelledDistance) {
        double kmFromLastPosition = distanceCalculator.calculateByGeo(
                latitude,
                longitude,
                travelledDistance.getLastLatitude(),
                travelledDistance.getLastLongitude()
        );

        Distance distanceFromLastPosition = Distance.ofKm(kmFromLastPosition);

        travelledDistance.addDistance(
                distanceFromLastPosition,
                latitude,
                longitude,
                lastPositionTime
        );
    }

    private TravelledDistance createTravelledDistance(Long driverId,
                                                      TimeSlot timeSlot,
                                                      double lastLatitude,
                                                      double lastLongitude,
                                                      Instant lastPositionTime) {
        TravelledDistance travelledDistance = new TravelledDistance(
                driverId,
                timeSlot,
                lastLatitude,
                lastLongitude,
                lastPositionTime
        );

        return travelledDistanceRepository.save(travelledDistance);
    }

    private void recalculateDistanceFor(TravelledDistance travelledDistance,
                                        TimeSlot timeSlot,
                                        Long driverId) {
        travelledDistanceRepository.delete(travelledDistance);

        List<DriverPosition> driverPositionsToRecalculate =
                driverPositionRepository.findByDriverIdAndSeenAtGreaterThanEqualAndSeenAtLessThanEqualOrderBySeenAtAsc(
                        driverId,
                        timeSlot.getBeginning(),
                        timeSlot.getEnd()
                );

        DriverPosition firstDriverPosition = driverPositionsToRecalculate.get(0);

        TravelledDistance recalculatedTravelledDistance = createTravelledDistance(
                driverId,
                timeSlot,
                firstDriverPosition.getLatitude(),
                firstDriverPosition.getLongitude(),
                firstDriverPosition.getSeenAt());

        driverPositionsToRecalculate.forEach(driverPosition -> {
                    if (!Objects.equals(driverPosition, firstDriverPosition)) {
                        addDistance(
                                driverPosition.getLatitude(),
                                driverPosition.getLongitude(),
                                driverPosition.getSeenAt(),
                                recalculatedTravelledDistance);
                    }
                }
        );
    }
}
