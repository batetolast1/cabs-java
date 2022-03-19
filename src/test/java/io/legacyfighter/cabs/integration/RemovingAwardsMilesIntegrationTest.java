package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.service.AwardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static io.legacyfighter.cabs.entity.Client.Type.NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
class RemovingAwardsMilesIntegrationTest {

    private static final Instant SUNDAY = LocalDateTime.of(2022, 3, 13, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant MONDAY = SUNDAY.plus(1, ChronoUnit.DAYS);
    private static final Instant TUESDAY = SUNDAY.plus(2, ChronoUnit.DAYS);
    private static final Instant WEDNESDAY = SUNDAY.plus(3, ChronoUnit.DAYS);

    @MockBean
    private Clock clock;

    @MockBean
    private AppProperties appProperties;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AwardsService awardsService;

    @Test
    void classCastExceptionIsThrownWhenComparableIsNotImplementedProperly() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        Long clientId = client.getId();
        // and
        fixtures.hasDoneClaims(client, 3);
        // and
        grantedNonExpiringMiles(client, 20, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        grantedNonExpiringMiles(client, 20, MONDAY);
        // and
        isMonday();

        // when
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> awardsService.removeMiles(clientId, 40));
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenClientHasManyClaims() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneClaims(client, 3);
        // and
        grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        grantedNonExpiringMiles(client, 20, MONDAY);

        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 40);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(10);
    }

    @Test
    void shouldRemoveNonExpiringMilesFirstWhenClientHasManyClaims() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneClaims(client, 3);
        // and
        grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        grantedNonExpiringMiles(client, 20, MONDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(25);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenClientIsVip() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(45);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenClientIsVip() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(20);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenManyTransitsAndIsSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(45);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(20);
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenManyTransitsAndIsNotSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(45);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsNotSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(20);
    }

    @Test
    void byDefaultRemoveOldestMilesFirstEvenIfTheyAreNonExpiring() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.NORMAL);
        // and
        grantedNonExpiringMiles(client, 15, SUNDAY);
        grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // then
        awardsService.removeMiles(client.getId(), 20);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(50);
    }

    Client aClientWithAnActiveMilesProgram(Client.Type type) {
        when(clock.instant()).thenReturn(SUNDAY);
        Client client = fixtures.aClient(type);
        fixtures.hasActiveAwardsAccount(client);
        return client;
    }

    void grantedMilesThatWillExpireInDays(Client client, int milesAmount, int expirationInDays, Instant at) {
        setMilesWillExpireInDays(expirationInDays);
        setDefaultMilesBonus(milesAmount);
        aMilesRegisteredAt(at, client);
    }

    void grantedNonExpiringMiles(Client client, int milesAmount, Instant at) {
        when(clock.instant()).thenReturn(at);
        setDefaultMilesBonus(milesAmount);
        awardsService.registerNonExpiringMiles(client.getId(), milesAmount);
    }

    void aMilesRegisteredAt(Instant at, Client client) {
        when(clock.instant()).thenReturn(at);
        Transit transit = fixtures.aCompletedTransitFor(client);
        awardsService.registerMiles(client.getId(), transit.getId());
    }

    void setMilesWillExpireInDays(int days) {
        when(appProperties.getMilesExpirationInDays()).thenReturn(days);
    }

    void setDefaultMilesBonus(int milesAmount) {
        when(appProperties.getDefaultMilesBonus()).thenReturn(milesAmount);
    }

    void isSunday() {
        when(clock.instant()).thenReturn(SUNDAY);
    }

    void isMonday() {
        when(clock.instant()).thenReturn(MONDAY);
    }
}