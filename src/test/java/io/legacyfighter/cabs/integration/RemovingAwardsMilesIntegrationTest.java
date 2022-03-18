package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.entity.miles.AwardedMiles;
import io.legacyfighter.cabs.repository.AwardedMilesRepository;
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
import java.util.List;
import java.util.Optional;

import static io.legacyfighter.cabs.entity.Client.Type.NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

/**
 * @deprecated use {@link io.legacyfighter.cabs.entity.miles.AwardsAccountTest}
 */
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
    private AwardedMilesRepository awardedMilesRepository;

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
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        AwardedMiles nonExpiringMiles = grantedNonExpiringMiles(client, 20, MONDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 40);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(firstToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(secondToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(thirdToExpire, 0, awardedMiles);
        assertThatMilesWereReducedTo(nonExpiringMiles, 0, awardedMiles);
    }

    @Test
    void shouldRemoveNonExpiringMilesFirstWhenClientHasManyClaims() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneClaims(client, 3);
        // and
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        AwardedMiles nonExpiringMiles = grantedNonExpiringMiles(client, 20, MONDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(firstToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(secondToExpire, 10, awardedMiles);
        assertThatMilesWereReducedTo(thirdToExpire, 10, awardedMiles);
        assertThatMilesWereReducedTo(nonExpiringMiles, 0, awardedMiles);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenClientIsVip() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenClientIsVip() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenManyTransitsAndIsSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenManyTransitsAndIsNotSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsNotSunday() {
        // given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        // and
        fixtures.hasDoneTransits(client, 15);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void byDefaultRemoveOldestMilesFirstEvenIfTheyAreNonExpiring() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.NORMAL);
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(client, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // then
        awardsService.removeMiles(client.getId(), 20);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundayNonExpiringMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondayNonExpiringMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 10, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    Client aClientWithAnActiveMilesProgram(Client.Type type) {
        when(clock.instant()).thenReturn(SUNDAY);
        Client client = fixtures.aClient(type);
        fixtures.hasActiveAwardsAccount(client);
        return client;
    }

    AwardedMiles grantedMilesThatWillExpireInDays(Client client, int milesAmount, int expirationInDays, Instant at) {
        setMilesWillExpireInDays(expirationInDays);
        setDefaultMilesBonus(milesAmount);
        return aMilesRegisteredAt(at, client);
    }

    AwardedMiles grantedNonExpiringMiles(Client client, int milesAmount, Instant at) {
        when(clock.instant()).thenReturn(at);
        setDefaultMilesBonus(milesAmount);
        return awardsService.registerNonExpiringMiles(client.getId(), milesAmount);
    }

    AwardedMiles aMilesRegisteredAt(Instant at, Client client) {
        when(clock.instant()).thenReturn(at);
        Transit transit = fixtures.aCompletedTransitFor(client);
        return awardsService.registerMiles(client.getId(), transit.getId());
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

    void assertThatMilesWereReducedTo(AwardedMiles awardedMiles, int milesAfterReduction, List<AwardedMiles> allMiles) {
        Optional<Integer> optionalAwardedMiles = allMiles.stream()
                .filter(am -> awardedMiles.getDate().equals(am.getDate())
                        && awardedMiles.getExpirationDate().equals(am.getExpirationDate()))
                .map(am -> am.getMilesAmount(Instant.MIN))
                .findFirst();

        assertThat(optionalAwardedMiles).isPresent();
        assertThat(optionalAwardedMiles).contains(milesAfterReduction);
    }
}