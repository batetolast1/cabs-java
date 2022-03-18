package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.entity.Client;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.legacyfighter.cabs.entity.Client.Type.NORMAL;
import static io.legacyfighter.cabs.entity.Client.Type.VIP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AwardsAccountTest {

    private static final Instant NOW = Instant.now();
    private static final Instant SUNDAY = LocalDateTime.of(2022, 3, 13, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant MONDAY = SUNDAY.plus(1, ChronoUnit.DAYS);
    private static final Instant TUESDAY = SUNDAY.plus(2, ChronoUnit.DAYS);
    private static final Instant WEDNESDAY = SUNDAY.plus(3, ChronoUnit.DAYS);

    @Test
    void canCreateNotActiveAwardsAccount() {
        // given
        Client client = new Client();

        // when
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(client, NOW);

        // then
        assertThat(awardsAccount.getClient()).isEqualTo(client);
        assertThat(awardsAccount.getDate()).isEqualTo(NOW);
        assertThat(awardsAccount.getTransactions()).isZero();
        assertThat(awardsAccount.isActive()).isFalse();
    }

    @Test
    void canActivateAccount() {
        // given
        Client client = new Client();
        Instant now = Instant.now();
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(client, now);

        // when
        awardsAccount.activate();

        // then
        assertThat(awardsAccount.isActive()).isTrue();
    }

    @Test
    void canDeactivateAccount() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();

        // when
        awardsAccount.deactivate();

        // then
        assertThat(awardsAccount.isActive()).isFalse();
    }

    @Test
    void canAddExpiringMiles() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();
        Instant yearAfter = now.plus(365, ChronoUnit.DAYS);

        // when
        awardsAccount.addExpiringMiles(10, now, yearAfter, null);

        // then
        assertThat(awardsAccount.getTransactions()).isOne();
        assertThat(awardsAccount.calculateBalance(now)).isEqualTo(ConstantUntil.constantUntil(10, now).getAmount(now));
        assertThat(awardsAccount.calculateBalance(yearAfter)).isEqualTo(ConstantUntil.constantUntil(10, now).getAmount(yearAfter));

    }

    @Test
    void canAddNonExpiringMiles() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();
        Instant yearAfter = now.plus(365, ChronoUnit.DAYS);

        // when
        awardsAccount.addNonExpiringMiles(10, now);

        // then
        assertThat(awardsAccount.getTransactions()).isOne();
        assertThat(awardsAccount.calculateBalance(now)).isEqualTo(ConstantUntil.constantUntilForever(10).getAmount(now));
        assertThat(awardsAccount.calculateBalance(yearAfter)).isEqualTo(ConstantUntil.constantUntilForever(10).getAmount(yearAfter));
    }

    @Test
    void canCalculateBalance() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();
        // and
        awardsAccount.addNonExpiringMiles(10, now);
        awardsAccount.addExpiringMiles(10, now, now.plus(365, ChronoUnit.DAYS), null);

        // when
        Integer balance = awardsAccount.calculateBalance(now);

        // then
        assertThat(balance).isEqualTo(ConstantUntil.constantUntilForever(10).getAmount(now) + ConstantUntil.constantUntil(10, now).getAmount(now));
    }

    @Test
    void canCalculateEmptyBalance() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();

        // when
        Integer balance = awardsAccount.calculateBalance(now);

        // then
        assertThat(balance).isZero();
    }

    @Test
    void cannotRemoveMilesWhenAccountIsNotActive() {
        // given
        AwardsAccount awardsAccount = anInactiveAwardsAccount();
        // and
        Instant now = Instant.now();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsAccount.remove(40, now, 10, 10, NORMAL, true));
    }

    @Test
    void cannotRemoveMilesWhenNotEnoughMiles() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsAccount.remove(40, now, 10, 10, NORMAL, true));
    }

    @Test
    void classCastExceptionIsThrownWhenComparableIsNotImplementedProperly() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        Instant now = Instant.now();
        Instant yearAfter = now.plus(365, ChronoUnit.DAYS);
        // and
        awardsAccount.addNonExpiringMiles(15, now);
        awardsAccount.addExpiringMiles(10, now, yearAfter, null);
        awardsAccount.addNonExpiringMiles(15, now);

        // when
        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> awardsAccount.remove(40, now, 10, 10, NORMAL, true));
    }

    AwardedMiles grantedMilesThatWillExpireInDays(AwardsAccount awardsAccount, int miles, int expirationInDays, Instant at) {
        return awardsAccount.addExpiringMiles(miles, at, at.plus(expirationInDays, ChronoUnit.DAYS), null);
    }

    AwardedMiles grantedNonExpiringMiles(AwardsAccount awardsAccount, int miles, Instant at) {
        return awardsAccount.addNonExpiringMiles(miles, at);
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenClientHasManyClaims() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 15, 365, MONDAY);
        AwardedMiles nonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 20, MONDAY);

        // when
        awardsAccount.remove(40, MONDAY, 0, 3, NORMAL, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(firstToExpire);
        assertThat(awardedMiles.get(1)).isEqualTo(secondToExpire);
        assertThat(awardedMiles.get(2)).isEqualTo(thirdToExpire);
        assertThat(awardedMiles.get(3)).isEqualTo(nonExpiringMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isZero();
    }

    @Test
    void shouldRemoveNonExpiringMilesFirstWhenClientHasManyClaims() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles firstToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 5, 10, MONDAY);
        AwardedMiles secondToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 10, 60, MONDAY);
        AwardedMiles thirdToExpire = grantedMilesThatWillExpireInDays(awardsAccount, 15, 365, MONDAY);
        AwardedMiles nonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 20, MONDAY);

        // when
        awardsAccount.remove(25, MONDAY, 0, 3, NORMAL, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(firstToExpire);
        assertThat(awardedMiles.get(1)).isEqualTo(secondToExpire);
        assertThat(awardedMiles.get(2)).isEqualTo(thirdToExpire);
        assertThat(awardedMiles.get(3)).isEqualTo(nonExpiringMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isEqualTo(10);
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isEqualTo(10);
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isZero();
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenClientIsVip() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(25, MONDAY, 15, 0, VIP, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isEqualTo(15);
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(25);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenClientIsVip() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(50, MONDAY, 15, 0, VIP, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(20);
    }

    @Test
    void shouldRemoveSoonToExpireMilesFirstWhenManyTransitsAndIsSunday() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(25, MONDAY, 15, 0, NORMAL, true);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isEqualTo(15);
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(25);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsSunday() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(50, MONDAY, 15, 0, NORMAL, true);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(20);
    }

    @Test
    void shouldRemoveLatestToExpireMilesFirstWhenManyTransitsAndIsNotSunday() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(25, MONDAY, 15, 0, NORMAL, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isEqualTo(15);
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isEqualTo(5);
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(25);
    }

    @Test
    void shouldRemoveNonExpiringMilesLastWhenManyTransitsAndIsNotSunday() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(50, MONDAY, 15, 0, NORMAL, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(20);
    }

    @Test
    void byDefaultRemoveOldestMilesFirstEvenIfTheyAreNonExpiring() {
        // given
        AwardsAccount awardsAccount = anActiveAwardsAccount();
        // and
        AwardedMiles sundayNonExpiringMiles = grantedNonExpiringMiles(awardsAccount, 15, SUNDAY);
        AwardedMiles mondayNonExpiringMiles = grantedMilesThatWillExpireInDays(awardsAccount, 20, 365, MONDAY);
        AwardedMiles tuesdayMiles = grantedMilesThatWillExpireInDays(awardsAccount, 10, 10, TUESDAY);
        AwardedMiles wednesdayMiles = grantedNonExpiringMiles(awardsAccount, 25, WEDNESDAY);

        // when
        awardsAccount.remove(20, MONDAY, 0, 0, NORMAL, false);

        // then
        List<AwardedMiles> awardedMiles = awardsAccount.getMiles();
        assertThat(awardedMiles.get(0)).isEqualTo(sundayNonExpiringMiles);
        assertThat(awardedMiles.get(1)).isEqualTo(mondayNonExpiringMiles);
        assertThat(awardedMiles.get(2)).isEqualTo(tuesdayMiles);
        assertThat(awardedMiles.get(3)).isEqualTo(wednesdayMiles);

        assertThat(awardedMiles.get(0).getMilesAmount(MONDAY)).isZero();
        assertThat(awardedMiles.get(1).getMilesAmount(MONDAY)).isEqualTo(15);
        assertThat(awardedMiles.get(2).getMilesAmount(MONDAY)).isEqualTo(10);
        assertThat(awardedMiles.get(3).getMilesAmount(MONDAY)).isEqualTo(25);
    }

    @Test
    void cannotTransferMilesWhenAccountIsNotActive() {
        // given
        AwardsAccount awardsAccountFrom = anInactiveAwardsAccount();
        AwardsAccount awardsAccountTo = anInactiveAwardsAccount();
        // and
        Instant now = Instant.now();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsAccountFrom.moveMilesTo(awardsAccountTo, 10, now));
    }

    @Test
    void cannotTransferMilesWhenNotEnoughMiles() {
        // given
        AwardsAccount awardsAccountFrom = anActiveAwardsAccount();
        AwardsAccount awardsAccountTo = anInactiveAwardsAccount();
        // and
        Instant now = Instant.now();

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsAccountFrom.moveMilesTo(awardsAccountTo, 10, now));
    }

    @Test
    void canTransferMiles() {
        // given
        AwardsAccount awardsAccountFrom = anActiveAwardsAccount();
        AwardsAccount awardsAccountTo = anInactiveAwardsAccount();
        // and
        Instant now = Instant.now();
        Instant yearAfter = now.plus(365, ChronoUnit.DAYS);
        // and
        awardsAccountFrom.addExpiringMiles(10, now, yearAfter, null);
        awardsAccountFrom.addNonExpiringMiles(15, now);
        awardsAccountFrom.addExpiringMiles(10, now, yearAfter, null);
        awardsAccountFrom.addNonExpiringMiles(20, now);

        // when
        awardsAccountFrom.moveMilesTo(awardsAccountTo, 27, now);

        // then
        assertThat(awardsAccountFrom.getTransactions()).isEqualTo(5);
        assertThat(awardsAccountFrom.calculateBalance(now)).isEqualTo(28);

        List<AwardedMiles> awardedMilesFrom = awardsAccountFrom.getMiles();
        assertThat(awardedMilesFrom).hasSize(2);
        AwardedMiles expectedExpiringMilesFrom = new AwardedMiles(null, now, ConstantUntil.constantUntil(8, yearAfter), awardsAccountFrom);
        assertThat(awardedMilesFrom.get(0)).isEqualTo(expectedExpiringMilesFrom);
        AwardedMiles expectedNonExpiringMilesFrom = new AwardedMiles(null, now, ConstantUntil.constantUntilForever(20), awardsAccountFrom);
        assertThat(awardedMilesFrom.get(1)).isEqualTo(expectedNonExpiringMilesFrom);

        assertThat(awardsAccountTo.getTransactions()).isEqualTo(1);
        assertThat(awardsAccountTo.calculateBalance(now)).isEqualTo(27);

        List<AwardedMiles> awardedMilesTo = awardsAccountTo.getMiles();
        assertThat(awardedMilesTo).hasSize(3);
        AwardedMiles expectedExpiringMilesTo = new AwardedMiles(null, now, ConstantUntil.constantUntil(10, yearAfter), awardsAccountTo);
        assertThat(awardedMilesTo.get(0)).isEqualTo(expectedExpiringMilesTo);
        AwardedMiles expectedNonExpiringMilesTo = new AwardedMiles(null, now, ConstantUntil.constantUntilForever(15), awardsAccountTo);
        assertThat(awardedMilesTo.get(1)).isEqualTo(expectedNonExpiringMilesTo);
        AwardedMiles expectedExpiringMilesTo2 = new AwardedMiles(null, now, ConstantUntil.constantUntil(2, yearAfter), awardsAccountTo);
        assertThat(awardedMilesTo.get(2)).isEqualTo(expectedExpiringMilesTo2);
    }

    private AwardsAccount anInactiveAwardsAccount() {
        Client client = new Client();
        Instant now = Instant.now();
        return AwardsAccount.notActiveAccount(client, now);
    }

    private AwardsAccount anActiveAwardsAccount() {
        AwardsAccount awardsAccount = anInactiveAwardsAccount();
        awardsAccount.activate();
        return awardsAccount;
    }
}
