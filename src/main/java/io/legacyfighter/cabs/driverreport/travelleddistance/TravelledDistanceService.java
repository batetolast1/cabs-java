package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.distance.Distance;
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
    public void addPosition(Long driverId,
                            double latitude,
                            double longitude,
                            Instant seenAt) {
        Instant now = Instant.now(clock);

        if (seenAt.isAfter(now)) {
            throw new IllegalArgumentException();
        }

        TravelledDistance matchedSlotForDriverPosition = travelledDistanceRepository.findTravelledDistanceByTimestampAndDriverId(seenAt, driverId);

        if (matchedSlotForDriverPosition != null) {
            if (matchedSlotForDriverPosition.contains(now)) {
                addDistanceToSlot(latitude, longitude, matchedSlotForDriverPosition);
            } else { // else is enough, see line 33
                recalculateDistanceFor(matchedSlotForDriverPosition, driverId); // TODO
            }
        } else {
            TimeSlot currentTimeSlot = TimeSlot.timeSlotThatContains(now);
            if (currentTimeSlot.contains(seenAt)) {
                createSlotFor(driverId, currentTimeSlot, latitude, longitude);

                TimeSlot previousTimeSlot = currentTimeSlot.previous();
                if (previousTimeSlot.endsAt(seenAt)) {
                    TravelledDistance previousTravelledDistance = travelledDistanceRepository.findTravelledDistanceByTimeSlotAndDriverId(previousTimeSlot, driverId);
                    if (previousTravelledDistance != null) {
                        addDistanceToSlot(latitude, longitude, previousTravelledDistance);
                    }
                }
            } else {
                TimeSlot timeSlotFromDriverPosition = TimeSlot.timeSlotThatContains(seenAt);
                createSlotFor(driverId, timeSlotFromDriverPosition, latitude, longitude);
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

    private void addDistanceToSlot(double latitude,
                                   double longitude,
                                   TravelledDistance aggregatedDistance) {
        double km = distanceCalculator.calculateByGeo(
                latitude,
                longitude,
                aggregatedDistance.getLastLatitude(),
                aggregatedDistance.getLastLongitude()
        );

        Distance travelledDistance = Distance.ofKm(km);

        aggregatedDistance.addDistance(
                travelledDistance,
                latitude,
                longitude
        );
    }

    private void createSlotFor(Long driverId,
                               TimeSlot timeSlot,
                               double lastLatitude,
                               double lastLongitude) {
        TravelledDistance travelledDistance = new TravelledDistance(
                driverId,
                timeSlot,
                lastLatitude,
                lastLongitude
        );

        travelledDistanceRepository.save(travelledDistance);
    }

    private void recalculateDistanceFor(TravelledDistance aggregatedDistance,
                                        Long driverId) {
        // TODO
    }
}
