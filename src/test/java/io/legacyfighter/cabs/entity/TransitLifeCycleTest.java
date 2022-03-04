package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.distance.Distance;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.*;

class TransitLifeCycleTest {

    @Test
    void canCreateTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();

        // when
        Transit transit = requestTransitFromTo(from, to);

        // then
        assertThat(transit.getClient()).isNotNull();
        assertThat(transit.getFrom()).isEqualTo(from);
        assertThat(transit.getTo()).isEqualTo(to);
        assertThat(transit.getCarType()).isNotNull();
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.DRAFT);
        assertThat(transit.getTariff()).isNotNull();
        assertThat(transit.getDateTime()).isEqualTo(transit.getDateTime());
        assertThat(transit.getKm()).isNotNull();

        assertThat(transit.getPrice()).isNull();
        assertThat(transit.getProposedDrivers()).isEmpty();
        assertThat(transit.getPrice()).isNull();
        assertThat(transit.getPublished()).isNull();
        assertThat(transit.getAcceptedAt()).isNull();
        assertThat(transit.getStarted()).isNull();
        assertThat(transit.getCompleteAt()).isNull();
        assertThat(transit.getAwaitingDriversResponses()).isZero();
        assertThat(transit.getPickupAddressChangeCounter()).isZero();
        assertThat(transit.getDriversRejections()).isEmpty();
    }

    @Test
    void canChangePickupPlaceWhenTransitIsCreated() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        transit.changePickupTo(newFrom, Distance.ofKm(10), 0);

        // then
        assertThat(transit.getFrom()).isEqualTo(newFrom);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void canChangePickupPlaceWhenTransitIsPublished() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        transit.changePickupTo(newFrom, Distance.ofKm(10), 0);

        // then
        assertThat(transit.getFrom()).isEqualTo(newFrom);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsCancelled() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.cancel();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(newFrom, Distance.ofKm(10), 10));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsAccepted() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(newFrom, Distance.ofKm(10), 10));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsInProgress() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(newFrom, Distance.ofKm(10), 10));
    }

    @Test
    void cannotChangePickupPlaceWhenTransitIsCompleted() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newFrom = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(newFrom, Distance.ofKm(10), 10));
    }

    @Test
    void cannotChangePickupPlaceMoreThanThreeTimes() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address from1 = aRandomAddress();
        Address from2 = aRandomAddress();
        Address from3 = aRandomAddress();
        Address from4 = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.changePickupTo(from1, Distance.ofKm(10), 0);
        transit.changePickupTo(from2, Distance.ofKm(10), 0);
        transit.changePickupTo(from3, Distance.ofKm(10), 0);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(from4, Distance.ofKm(10), 0));
    }

    @Test
    void cannotChangePickupPlaceWhenItIsFarAwayFromOriginalPickupPlace() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        Address farAway = aRandomAddress();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changePickupTo(farAway, Distance.ofKm(10), 10));
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsCreated() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        transit.changeDestinationTo(newTo, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isEqualTo(newTo);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsPublished() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        transit.changeDestinationTo(newTo, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isEqualTo(newTo);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsCancelled() { // weird
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.cancel();

        // when
        transit.changeDestinationTo(newTo, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isEqualTo(newTo);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsAccepted() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        transit.changeDestinationTo(newTo, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isEqualTo(newTo);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void canChangeTransitDestinationWhenTransitIsStarted() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.proposeTo(driver);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        transit.changeDestinationTo(newTo, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isEqualTo(newTo);
        assertThat(transit.getKm()).isNotNull();
    }

    @Test
    void cannotChangeDestinationWhenTransitIsCompleted() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        Address newTo = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.changeDestinationTo(newTo, Distance.ofKm(10)));
    }

    @Test
    void cannotCancelTransitWhenTransitIsCancelled() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.cancel();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::canCancel);
    }


    @Test
    void canCancelCreatedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        transit.cancel();

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }


    @Test
    void cannotCancelTransitWhenDriverAssignmentFailed() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.failDriverAssignment();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::canCancel);
    }

    @Test
    void canCancelPublishedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        transit.cancel();

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }

    @Test
    void canCancelAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        transit.cancel();

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.CANCELLED);
    }

    @Test
    void cannotCancelStartedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::canCancel);
    }

    @Test
    void cannotCancelCompletedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(transit::canCancel);
    }

    @Test
    void canPublishCreatedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void canPublishCancelledTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // adn
        transit.cancel();

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void canPublishPublishedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void canPublishAcceptedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void canPublishStartedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void canPublishCompletedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        transit.publishAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getPublished()).isNotNull();
    }

    @Test
    void onlyOneDriverCanAcceptTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void transitCannotByAcceptedByDriverWhoHasNotSeenProposal() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void transitCannotByAcceptedByDriverWhoAlreadyRejected() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.rejectBy(driver);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void canAcceptPublishedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);

        // when
        transit.acceptBy(driver, Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(transit.getAcceptedAt()).isNotNull();
    }

    @Test
    void canAcceptCancelledTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.cancel();

        // when
        transit.acceptBy(driver, Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(transit.getAcceptedAt()).isNotNull();
    }

    @Test
    void cannotAcceptAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void cannotAcceptStartedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void cannotAcceptCompletedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.acceptBy(driver, Instant.now()));
    }

    @Test
    void cannotStartNotAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.startAt(Instant.now()));
    }

    @Test
    void canStartAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        transit.startAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.IN_TRANSIT);
        assertThat(transit.getStarted()).isNotNull();
    }

    @Test
    void cannotStartCancelledTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.cancel();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.startAt(Instant.now()));
    }

    @Test
    void cannotStartStartedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.startAt(Instant.now()));
    }

    @Test
    void cannotStartCompletedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> transit.startAt(Instant.now()));
    }

    @Test
    void sameDriverWhoAcceptedAndThenRejectedCanStartTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.rejectBy(driver);

        // when
        transit.startAt(Instant.now());

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.IN_TRANSIT);
        assertThat(transit.getAcceptedAt()).isNotNull();
    }

    @Test
    void canRejectDraftTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        transit.rejectBy(driver);

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.DRAFT);
        assertThat(transit.getAcceptedAt()).isNull();
    }

    @Test
    void canRejectPublishedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        transit.rejectBy(driver);

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.WAITING_FOR_DRIVER_ASSIGNMENT);
        assertThat(transit.getAcceptedAt()).isNull();
    }

    @Test
    void canRejectAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        transit.rejectBy(driver);

        // then
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.TRANSIT_TO_PASSENGER);
        assertThat(transit.getAcceptedAt()).isNotNull();
    }

    @Test
    void canRejectStartedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        assertThatNoException().isThrownBy(() -> transit.rejectBy(driver));
    }

    @Test
    void canRejectCompletedTransit() { // weird?
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());
        // and
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // when
        assertThatNoException().isThrownBy(() -> transit.rejectBy(driver));
    }

    @Test
    void cannotCompleteCreatedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transit.completeAt(Instant.now(), to, Distance.ofKm(10)));
    }

    @Test
    void cannotCompletePublishedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transit.completeAt(Instant.now(), to, Distance.ofKm(10)));
    }

    @Test
    void cannotCompleteCancelledTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.cancel();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transit.completeAt(Instant.now(), to, Distance.ofKm(10)));
    }

    @Test
    void cannotCompleteAcceptedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transit.completeAt(Instant.now(), to, Distance.ofKm(10)));
    }

    @Test
    void canCompleteStartedTransit() {
        // given
        Address from = aRandomAddress();
        Address to = aRandomAddress();
        // and
        Driver driver = new Driver();
        // and
        Transit transit = requestTransitFromTo(from, to);
        // and
        transit.publishAt(Instant.now());
        // and
        transit.proposeTo(driver);
        // and
        transit.acceptBy(driver, Instant.now());
        // and
        transit.startAt(Instant.now());

        // when
        transit.completeAt(Instant.now(), to, Distance.ofKm(10));

        // then
        assertThat(transit.getTo()).isNotNull();
        assertThat(transit.getKm()).isNotNull();
        assertThat(transit.getStatus()).isEqualTo(Transit.Status.COMPLETED);
        assertThat(transit.getPrice()).isNotNull();
        assertThat(transit.getCompleteAt()).isNotNull();
    }

    private Transit requestTransitFromTo(Address pickup, Address destination) {
        return new Transit(pickup, destination, new Client(), CarType.CarClass.VAN, now(), Distance.ZERO);
    }

    private Address aRandomAddress() {
        return new Address("country", "city", UUID.randomUUID().toString(), 1);
    }
}
