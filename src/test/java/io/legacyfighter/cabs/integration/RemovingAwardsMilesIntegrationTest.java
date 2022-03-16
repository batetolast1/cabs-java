package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.entity.AwardedMiles;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
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
import java.util.Objects;
import java.util.Optional;

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
    private AwardedMilesRepository awardedMilesRepository;

    @Autowired
    private AwardsService awardsService;

    @Test
    void classCastExceptionIsThrownWhenClientHasManyClaimsAndWeirdMixOfAwardedMiles() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        Long clientId = client.getId();
        //and
        fixtures.hasDoneClaims(client, 3);
        //and
        grantedSpecialMiles(client, 20, MONDAY);
        grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        grantedSpecialMiles(client, 20, MONDAY);
        // and
        isMonday();

        //when
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> awardsService.removeMiles(clientId, 40));
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenClientHasManyClaims() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneClaims(client, 3);
        //and
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        AwardedMiles specialMiles = grantedSpecialMiles(client, 20, MONDAY);
        // and
        isMonday();

        //when
        awardsService.removeMiles(client.getId(), 40);

        //then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(firstToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(secondToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(thirdToExpire, 0, awardedMiles);
        assertThatMilesWereReducedTo(specialMiles, 0, awardedMiles);
    }

    @Test
    void shouldRemoveSpecialMilesFirstWhenClientHasManyClaims() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneClaims(client, 3);
        //and
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(client, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(client, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(client, 15, 365, MONDAY);
        AwardedMiles specialMiles = grantedSpecialMiles(client, 20, MONDAY);
        // and
        isMonday();

        //when
        awardsService.removeMiles(client.getId(), 25);

        //then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(firstToExpire, 5, awardedMiles);
        assertThatMilesWereReducedTo(secondToExpire, 10, awardedMiles);
        assertThatMilesWereReducedTo(thirdToExpire, 10, awardedMiles);
        assertThatMilesWereReducedTo(specialMiles, 0, awardedMiles);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenClientIsVip() {
        //given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveSpecialMilesLastWhenClientIsVip() {
        //given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.VIP);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenManyTransitsAndIsSunday() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveSpecialMilesLastWhenManyTransitsAndIsSunday() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isSunday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenManyTransitsAndIsNotSunday() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 25);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 5, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    @Test
    void shouldRemoveSpecialMilesLastWhenManyTransitsAndIsNotSunday() {
        //given
        Client client = aClientWithAnActiveMilesProgram(NORMAL);
        //and
        fixtures.hasDoneTransits(client, 15);
        //and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // when
        awardsService.removeMiles(client.getId(), 50);

        // then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 20, awardedMiles);
    }

    @Test
    void byDefaultRemoveOldestMilesFirstEvenIfTheyAreSpecial() {
        // given
        Client client = aClientWithAnActiveMilesProgram(Client.Type.NORMAL);
        // and
        AwardedMiles sundaySpecialMiles = grantedSpecialMiles(client, 15, SUNDAY);
        AwardedMiles mondaySpecialMiles = grantedMilesThatWillExpireInDays(client, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(client, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedSpecialMiles(client, 25, WEDNESDAY);
        // and
        isMonday();

        // then
        awardsService.removeMiles(client.getId(), 20);

        //then
        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThatMilesWereReducedTo(sundaySpecialMiles, 0, awardedMiles);
        assertThatMilesWereReducedTo(mondaySpecialMiles, 15, awardedMiles);
        assertThatMilesWereReducedTo(tuesdayMiles, 10, awardedMiles);
        assertThatMilesWereReducedTo(wednesdayMiles, 25, awardedMiles);
    }

    Client aClientWithAnActiveMilesProgram(Client.Type type) {
        when(clock.instant()).thenReturn(SUNDAY);
        Client client = fixtures.aClient(type);
        fixtures.hasActiveAwardsAccount(client);
        return client;
    }

    AwardedMiles grantedMilesThatWillExpireInDays(Client client, int miles, int expirationInDays, Instant when) {
        setMilesWillExpireInDays(expirationInDays);
        setDefaultMilesBonus(miles);
        return aMilesRegisteredAt(when, client);
    }

    AwardedMiles grantedSpecialMiles(Client client, int miles, Instant when) {
        when(clock.instant()).thenReturn(when);
        setDefaultMilesBonus(miles);
        return awardsService.registerSpecialMiles(client.getId(), miles);
    }

    AwardedMiles aMilesRegisteredAt(Instant when, Client client) {
        when(clock.instant()).thenReturn(when);
        Transit transit = fixtures.aCompletedTransitFor(client);
        return awardsService.registerMiles(client.getId(), transit.getId());
    }

    void setMilesWillExpireInDays(int days) {
        when(appProperties.getMilesExpirationInDays()).thenReturn(days);
    }

    void setDefaultMilesBonus(int miles) {
        when(appProperties.getDefaultMilesBonus()).thenReturn(miles);
    }

    void isSunday() {
        when(clock.instant()).thenReturn(SUNDAY);
    }

    void isMonday() {
        when(clock.instant()).thenReturn(MONDAY);
    }

    void assertThatMilesWereReducedTo(AwardedMiles awardedMiles, int milesAfterReduction, List<AwardedMiles> allMiles) {
        Optional<AwardedMiles> optionalAwardedMiles = allMiles.stream()
                .filter(miles -> Objects.equals(awardedMiles.getId(), miles.getId()))
                .findFirst();
        assertThat(optionalAwardedMiles).isPresent();
        assertThat(optionalAwardedMiles.get().getMiles()).isEqualTo(milesAfterReduction);
    }
}