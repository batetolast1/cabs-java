package io.legacyfighter.cabs.driverreport.travelleddistance;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverPosition;
import io.legacyfighter.cabs.repository.DriverPositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class PopulateTravelledDistanceServiceIntegrationTest {

    private static final Instant _12_00 = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant _12_05 = _12_00.plus(5, ChronoUnit.MINUTES);
    private static final Instant _12_15 = _12_00.plus(15, ChronoUnit.MINUTES);

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private DriverPositionRepository driverPositionRepository;

    @Autowired
    private TravelledDistanceRepository travelledDistanceRepository;

    @Autowired
    private TravelledDistanceService travelledDistanceService;

    @MockBean
    private Clock clock;

    @Autowired
    private PopulateTravelledDistanceService populateTravelledDistanceService;

    @Test
    void canPopulateTravelledDistanceFromDriverPosition() {
        // given
        Driver driver = fixtures.aDriver();
        // and
        registerPositionForDriver(driver, 53.4, -1.73, _12_00.plus(1, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.5, -1.74, _12_00.plus(2, ChronoUnit.MINUTES));
        registerPositionForDriver(driver, 53.6, -1.75, _12_00.plus(3, ChronoUnit.MINUTES));
        // and
        nowIs(_12_15);

        // when
        populateTravelledDistanceService.populate();

        // then
        TravelledDistance travelledDistance = travelledDistanceRepository.findTravelledDistanceByTimestampAndDriverId(_12_00, driver.getId());

        assertThat(travelledDistance.getDistance()).isEqualTo(Distance.ofKm(22.278292888707547));
        assertThat(travelledDistance.getLastPositionTime()).isEqualTo(_12_00.plus(3, ChronoUnit.MINUTES));
        assertThat(travelledDistance.getLastLatitude()).isEqualTo(53.6);
        assertThat(travelledDistance.getLastLongitude()).isEqualTo(-1.75);
        // and
        Distance distance = travelledDistanceService.calculateDistance(driver.getId(), _12_00, _12_05);

        assertThat(distance.printIn("km")).isEqualTo("22.278km");
    }

    private void nowIs(Instant instant) {
        when(clock.instant()).thenReturn(instant);
    }

    private void registerPositionForDriver(Driver driver,
                                           double latitude,
                                           double longitude,
                                           Instant seenAt) {
        driverPositionRepository.save(new DriverPosition(driver, seenAt, latitude, longitude));
    }
}
