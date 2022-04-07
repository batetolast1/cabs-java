package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.service.DistanceCalculator;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;

@Service
public class TravelledDistanceService {

    private final TravelledDistanceRepository travelledDistanceRepository;

    private final Clock clock;

    private final DistanceCalculator distanceCalculator;

    public TravelledDistanceService(TravelledDistanceRepository travelledDistanceRepository,
                                    Clock clock,
                                    DistanceCalculator distanceCalculator) {
        this.travelledDistanceRepository = travelledDistanceRepository;
        this.clock = clock;
        this.distanceCalculator = distanceCalculator;
    }

    @Transactional
    public void addPosition(DriverPosition driverPosition) {
        Instant now = Instant.now(clock);

//        if (!driverPosition.getSeenAt().isBefore(now)) {
//            throw new IllegalArgumentException();
//        }

        Long driverId = driverPosition.getDriver().getId();
        TravelledDistance matchedSlotForDriverPosition = travelledDistanceRepository.findTravelledDistanceByTimestampAndDriverId(driverPosition.getSeenAt(), driverId);

        if (matchedSlotForDriverPosition != null) {
            if (matchedSlotForDriverPosition.contains(now)) {
                addDistanceToSlot(driverPosition, matchedSlotForDriverPosition);
            } else { // else is enough, see line 33
                recalculateDistanceFor(matchedSlotForDriverPosition, driverId); // TODO
            }
        } else {
            TimeSlot currentTimeSlot = TimeSlot.timeSlotThatContains(now);
            if (currentTimeSlot.contains(driverPosition.getSeenAt())) {
                createSlotFor(driverId, currentTimeSlot, driverPosition);

                TimeSlot previousTimeSlot = currentTimeSlot.previous();
                if (previousTimeSlot.endsAt(driverPosition.getSeenAt())) {
                    TravelledDistance previousTravelledDistance = travelledDistanceRepository.findTravelledDistanceByTimeSlotAndDriverId(previousTimeSlot, driverId);
                    if (previousTravelledDistance != null) {
                        addDistanceToSlot(driverPosition, previousTravelledDistance);
                    }
                }
            } else {
                TimeSlot timeSlotFromDriverPosition = TimeSlot.timeSlotThatContains(driverPosition.getSeenAt());
                createSlotFor(driverId, timeSlotFromDriverPosition, driverPosition);
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

    private void addDistanceToSlot(DriverPosition driverPosition,
                                   TravelledDistance aggregatedDistance) {
        double km = distanceCalculator.calculateByGeo(
                driverPosition.getLatitude(),
                driverPosition.getLongitude(),
                aggregatedDistance.getLastLatitude(),
                aggregatedDistance.getLastLongitude()
        );

        Distance travelledDistance = Distance.ofKm(km);

        aggregatedDistance.addDistance(
                travelledDistance,
                driverPosition.getLatitude(),
                driverPosition.getLongitude()
        );
    }

    private void createSlotFor(Long driverId,
                               TimeSlot timeSlot,
                               DriverPosition driverPosition) {
        travelledDistanceRepository.save(new TravelledDistance(driverId, timeSlot, driverPosition));
    }

    private void recalculateDistanceFor(TravelledDistance aggregatedDistance, Long driverId) {
        // TODO
    }
}
