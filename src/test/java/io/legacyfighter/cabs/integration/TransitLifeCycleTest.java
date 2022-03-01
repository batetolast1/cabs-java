package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.AddressDTO;
import io.legacyfighter.cabs.dto.ClientDTO;
import io.legacyfighter.cabs.dto.TransitDTO;
import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.DriverFee;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.service.DriverSessionService;
import io.legacyfighter.cabs.service.DriverTrackingService;
import io.legacyfighter.cabs.service.GeocodingService;
import io.legacyfighter.cabs.service.TransitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

import static io.legacyfighter.cabs.entity.CarType.CarClass.VAN;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TransitLifeCycleTest {

    @Autowired
    Fixtures fixtures;

    @Autowired
    TransitService transitService;

    @MockBean
    GeocodingService geocodingService;

    @Autowired
    DriverSessionService driverSessionService;

    @Autowired
    DriverTrackingService driverTrackingService;

    @BeforeEach
    public void setup() {
        fixtures.anActiveCarCategory(VAN);

        when(geocodingService.geocodeAddress(any(Address.class))).thenReturn(new double[]{1, 1});
    }

    @Test
    void canCreateTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        TransitDTO transitDTO = fixtures.aTransitDTO(from, to);

        // when
        Transit transit = transitService.createTransit(transitDTO);

        // then
        TransitDTO loaded = transitService.loadTransit(transit.getId());
        assertThat(loaded.getKmRate()).isNotZero();
        assertThat(loaded.getTariff()).isNotNull();
        assertThat(loaded.getDistance("km")).isEqualTo(transit.getKm().printIn("km"));
        assertThat(loaded.getProposedDrivers()).isEmpty();
        assertThat(loaded.getClaimDTO()).isNull();
        assertThat(loaded.getTo()).isEqualTo(to);
        assertThat(loaded.getFrom()).isEqualTo(from);
        assertThat(loaded.getCarClass()).isNull();
        assertThat(loaded.getClientDTO()).isEqualTo(new ClientDTO(transit.getClient()));
        assertThat(loaded.getId()).isEqualTo(transit.getId());
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.DRAFT);
        assertThat(loaded.getPrice()).isNull();
        assertThat(loaded.getDriverFee()).isNull();
        assertThat(loaded.getDateTime()).isEqualTo(transit.getDateTime());
        assertThat(loaded.getPublished()).isNull();
        assertThat(loaded.getAcceptedAt()).isNull();
        assertThat(loaded.getStarted()).isNull();
        assertThat(loaded.getCompleteAt()).isNull();
        assertThat(loaded.getEstimatedPrice().intValue()).isEqualTo(transit.getEstimatedPrice().toInt());
    }

    @Test
    void cannotChangePickupPlaceWhenTransitDoesntExist() {
        // given
        AddressDTO from = aRandomAddress();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(0L, from));
    }

    @Test
    void canChangePickupPlaceWhenTransitIsCreated() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.changeTransitAddressFrom(transitId, newFrom);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getFrom()).isEqualTo(newFrom);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void canChangePickupPlaceWhenTransitIsPublished() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.changeTransitAddressFrom(transitId, newFrom);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getFrom()).isEqualTo(newFrom);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsCancelled() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.cancelTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, newFrom));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsAccepted() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, newFrom));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsInProgress() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, newFrom));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsCompleted() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newFrom = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, newFrom));
    }

    @Test
    void cannotChangePickupPlaceMoreThanThreeTimes() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO from1 = aRandomAddress();
        AddressDTO from2 = aRandomAddress();
        AddressDTO from3 = aRandomAddress();
        AddressDTO from4 = aRandomAddress();

        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.changeTransitAddressFrom(transitId, from1);
        transitService.changeTransitAddressFrom(transitId, from2);
        transitService.changeTransitAddressFrom(transitId, from3);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, from4));
    }

    @Test
    void cannotChangePickupPlaceWhenItIsFarAwayFromOriginalPickupPlace() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        Long transitId = transit.getId();
        // and
        AddressDTO farAway = aFarAwayAddress(transit);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressFrom(transitId, farAway));
    }

    @Test
    void cannotChangeDestinationWhenTransitDoesntExist() {
        // given
        AddressDTO to = aRandomAddress();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.changeTransitAddressTo(0L, to));
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsCreated() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newTo = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.changeTransitAddressTo(transitId, newTo);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isEqualTo(newTo);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsPublished() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newTo = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.changeTransitAddressTo(transitId, newTo);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isEqualTo(newTo);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsCancelled() { // weird
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newTo = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.cancelTransit(transitId);

        // when
        transitService.changeTransitAddressTo(transitId, newTo);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isEqualTo(newTo);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsAccepted() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newTo = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        transitService.changeTransitAddressTo(transitId, newTo);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isEqualTo(newTo);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsStarted() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        AddressDTO newTo = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        transitService.changeTransitAddressTo(transitId, newTo);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isEqualTo(newTo);
        assertThat(loaded.getDistance("km")).isNotNull();
    }

    @Test
    void cannotChangeDestinationWhenTransitIsCompleted() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.changeTransitAddressTo(transitId, from));
    }

    @Test
    void cannotCancelNonExistingTransit() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.cancelTransit(0L));
    }

    @Test
    void cannotCancelTransitWhenTransitIsCancelled() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.cancelTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.cancelTransit(transitId));
    }


    @Test
    void canCancelCreatedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.cancelTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }


    @Test
    void cannotCancelTransitWhenDriverAssignmentFailed() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.cancelTransit(transitId));
    }

    @Test
    void canCancelPublishedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.cancelTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }

    @Test
    void canCancelAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        transitService.cancelTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }

    @Test
    void cannotCancelStartedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.cancelTransit(transitId));
    }

    @Test
    void cannotCancelCompletedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.cancelTransit(transitId));
    }

    @Test
    void cannotPublishNonExistingTransit() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.publishTransit(0L));

    }

    @Test
    void transitPublishedWhenNoDriversAreAvailableIsNotAssigned() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.DRIVER_ASSIGNMENT_FAILED);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishCreatedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishCancelledTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // adn
        transitService.cancelTransit(transitId);

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishPublishedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishAcceptedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishStartedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void canPublishCompletedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        transitService.publishTransit(transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getPublished()).isNotNull();
    }

    @Test
    void cannotAcceptNonExistingTransit() {
        // given
        Long driverId = aFarAwayDriver();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, 0L));
    }

    @Test
    void onlyOneDriverCanAcceptTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void transitCannotByAcceptedByDriverWhoHasNotSeenProposal() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aFarAwayDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void transitCannotByAcceptedByDriverWhoAlreadyRejected() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.rejectTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void canAcceptPublishedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.acceptTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(loaded.getAcceptedAt()).isNotNull();
    }

    @Test
    void canAcceptCancelledTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.cancelTransit(transitId);

        // when
        transitService.acceptTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(loaded.getAcceptedAt()).isNotNull();
    }

    @Test
    void cannotAcceptAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void cannotAcceptStartedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void cannotAcceptCompletedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.acceptTransit(driverId, transitId));
    }

    @Test
    void cannotStartNotExistingTransit() {
        // given
        Long driverId = aNearbyDriver();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.rejectTransit(driverId, 0L));
    }

    @Test
    void cannotStartNotAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.startTransit(driverId, transitId));
    }

    @Test
    void canStartAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        transitService.startTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.IN_TRANSIT);
        assertThat(loaded.getStarted()).isNotNull();
    }

    @Test
    void cannotStartCancelledTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.cancelTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.startTransit(driverId, transitId));
    }

    @Test
    void cannotStartStartedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.startTransit(driverId, transitId));
    }

    @Test
    void cannotStartCompletedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transitService.startTransit(driverId, transitId));
    }

    @Test
    void sameDriverWhoAcceptedAndThenRejectedCanStartTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.rejectTransit(driverId, transitId);

        // when
        transitService.startTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.IN_TRANSIT);
        assertThat(loaded.getAcceptedAt()).isNotNull();
    }

    @Test
    void cannotRejectNonExistingTransit() {
        // given
        Long driverId = aNearbyDriver();
        // and
        Long transitId = 0L;

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.rejectTransit(driverId, transitId));
    }

    @Test
    void canRejectDraftTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        transitService.rejectTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.DRAFT);
        assertThat(loaded.getAcceptedAt()).isNull();
    }

    @Test
    void canRejectPublishedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        transitService.rejectTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(loaded.getAcceptedAt()).isNull();
    }

    @Test
    void canRejectAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        transitService.rejectTransit(driverId, transitId);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(loaded.getAcceptedAt()).isNotNull();
    }

    @Test
    void canRejectStartedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        assertThatNoException().isThrownBy(() -> transitService.rejectTransit(driverId, transitId));
    }

    @Test
    void canRejectCompletedTransit() { // weird?
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);
        // and
        transitService.completeTransit(driverId, transitId, to);

        // when
        assertThatNoException().isThrownBy(() -> transitService.rejectTransit(driverId, transitId));
    }

    @Test
    void cannotCompleteNotExistingTransit() {
        // given
        Address toAddress = aRandomAddress().toAddressEntity();
        // and
        Long driverId = aNearbyDriver();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> transitService.completeTransit(driverId, 0L, toAddress));
    }

    @Test
    void cannotCompleteCreatedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        Address toAddress = to.toAddressEntity();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transitService.completeTransit(driverId, transitId, toAddress));
    }

    @Test
    void cannotCompletePublishedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        Address toAddress = to.toAddressEntity();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transitService.completeTransit(driverId, transitId, toAddress));
    }

    @Test
    void cannotCompleteCancelledTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        Address toAddress = to.toAddressEntity();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.cancelTransit(transitId);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transitService.completeTransit(driverId, transitId, toAddress));
    }

    @Test
    void cannotCompleteAcceptedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        Address toAddress = to.toAddressEntity();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transitService.completeTransit(driverId, transitId, toAddress));
    }

    @Test
    void canCompleteStartedTransit() {
        // given
        AddressDTO from = aRandomAddress();
        AddressDTO to = aRandomAddress();
        // and
        Long driverId = aNearbyDriver();
        // and
        Long transitId = requestTransitFromTo(from, to).getId();
        // and
        transitService.publishTransit(transitId);
        // and
        transitService.acceptTransit(driverId, transitId);
        // and
        transitService.startTransit(driverId, transitId);

        // when
        transitService.completeTransit(driverId, transitId, to);

        // then
        TransitDTO loaded = transitService.loadTransit(transitId);
        assertThat(loaded.getTo()).isNotNull();
        assertThat(loaded.getDistance("km")).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo(Transit.Status.COMPLETED);
        assertThat(loaded.getPrice()).isNotNull();
        assertThat(loaded.getCompleteAt()).isNotNull();
        assertThat(loaded.getDriverFee()).isNotNull();
    }

    private AddressDTO aFarAwayAddress(Transit t) {
        AddressDTO addressDTO = new AddressDTO("country", "city", UUID.randomUUID().toString(), 1);

        when(geocodingService.geocodeAddress(any())).thenReturn(new double[]{1000, 1000});
        when(geocodingService.geocodeAddress(t.getFrom())).thenReturn(new double[]{1, 1});

        return addressDTO;
    }

    private Long aNearbyDriver() {
        Driver driver = fixtures.aDriver();

        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 100, new Money(0));
        driverSessionService.logIn(driver.getId(), "plateNumber", VAN, null);
        driverTrackingService.registerPosition(driver.getId(), 1, 1);

        return driver.getId();
    }

    private Long aFarAwayDriver() {
        Driver driver = fixtures.aDriver();

        fixtures.driverHasFee(driver, DriverFee.FeeType.FLAT, 100, new Money(0));
        driverSessionService.logIn(driver.getId(), "plateNumber", VAN, null);
        driverTrackingService.registerPosition(driver.getId(), 1000, 1000);

        return driver.getId();
    }

    private Transit requestTransitFromTo(AddressDTO from, AddressDTO to) {
        return transitService.createTransit(fixtures.aTransitDTO(from, to));
    }

    private AddressDTO aRandomAddress() {
        return new AddressDTO("country", "city", UUID.randomUUID().toString(), 1);
    }
}
