package io.legacyfighter.cabs.entity.miles;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AwardedMilesRemoveStrategyTest {

    private static final Instant SUNDAY = LocalDateTime.of(2022, 3, 13, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant MONDAY = SUNDAY.plus(1, ChronoUnit.DAYS);
    private static final Instant TUESDAY = SUNDAY.plus(2, ChronoUnit.DAYS);
    private static final Instant WEDNESDAY = SUNDAY.plus(3, ChronoUnit.DAYS);

    @Test
    void whenManyClaimsShouldSortLatestToExpireMilesFirstAndNonExpiringLast() {
        // given
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(15, 365, MONDAY);
        AwardedMiles nonExpiringMiles = grantedNonExpiringMiles(20, MONDAY);
        // and
        List<AwardedMiles> awardedMiles = new ArrayList<>();
        awardedMiles.add(firstToExpire);
        awardedMiles.add(secondToExpire);
        awardedMiles.add(thirdToExpire);
        awardedMiles.add(nonExpiringMiles);

        // when
        awardedMiles.sort(new TooManyClaimsRemoveStrategy());

        // then
        assertThat(awardedMiles.get(0)).isEqualTo(nonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(thirdToExpire);
        assertThat(awardedMiles.get(2)).isEqualTo(secondToExpire);
        assertThat(awardedMiles.get(3)).isEqualTo(firstToExpire);
    }

    @Test
    void whenVipShouldSortSoonToExpireFirstAndNonExpiringLast() {
        // given
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(15, SUNDAY);
        AwardedMiles mondayExpiringMiles = grantedMilesThatWillExpireInDays(20, 365, MONDAY);
        AwardedMiles tuesdayExpiringMiles = grantedMilesThatWillExpireInDays(10, 10, TUESDAY);
        AwardedMiles wednesdayNonExpiringMiles = grantedNonExpiringMiles(25, WEDNESDAY);
        // and
        List<AwardedMiles> awardedMiles = new ArrayList<>();
        awardedMiles.add(sundayNonExpiringMiles);
        awardedMiles.add(mondayExpiringMiles);
        awardedMiles.add(tuesdayExpiringMiles);
        awardedMiles.add(wednesdayNonExpiringMiles);

        // when
        awardedMiles.sort(new VipClientRemoveStrategy());

        // then
        assertThat(awardedMiles.get(0)).isEqualTo(tuesdayExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayNonExpiringMiles);
    }

    @Test
    void whenNormalClientShouldSortLatestToExpireFirstAndNonExpiringLast() {
        // given
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(15, SUNDAY);
        AwardedMiles mondayExpiringMiles = grantedMilesThatWillExpireInDays(20, 365, MONDAY);
        AwardedMiles tuesdayExpiringMiles = grantedMilesThatWillExpireInDays(10, 10, TUESDAY);
        AwardedMiles wednesdayNonExpiringMiles = grantedNonExpiringMiles(25, WEDNESDAY);
        // and
        List<AwardedMiles> awardedMiles = new ArrayList<>();
        awardedMiles.add(sundayNonExpiringMiles);
        awardedMiles.add(mondayExpiringMiles);
        awardedMiles.add(tuesdayExpiringMiles);
        awardedMiles.add(wednesdayNonExpiringMiles);

        // when
        awardedMiles.sort(new NormalClientRemoveStrategy());

        // then
        assertThat(awardedMiles.get(0)).isEqualTo(mondayExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(tuesdayExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayNonExpiringMiles);
    }

    @Test
    void byDefaultSortByOldestMilesFirstEvenIfTheyAreNonExpiring() {
        // given
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(25, WEDNESDAY);
        // and
        List<AwardedMiles> awardedMiles = new ArrayList<>();
        awardedMiles.add(sundayNonExpiringMiles);
        awardedMiles.add(mondayNonExpiringMiles);
        awardedMiles.add(tuesdayMiles);
        awardedMiles.add(wednesdayMiles);

        // when
        awardedMiles.sort(new DefaultRemoveStrategy());

        // then
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);
    }

    AwardedMiles grantedMilesThatWillExpireInDays(int milesAmount, int expirationInDays, Instant at) {
        Miles expiringMiles = ConstantUntil.constantUntil(milesAmount, at.plus(expirationInDays, ChronoUnit.DAYS));
        return new AwardedMiles(null, at, expiringMiles, new AwardsAccount());
    }

    AwardedMiles grantedNonExpiringMiles(int milesAmount, Instant at) {
        Miles nonExpiringMiles = ConstantUntil.constantUntilForever(milesAmount);
        return new AwardedMiles(null, at, nonExpiringMiles, new AwardsAccount());
    }
}
