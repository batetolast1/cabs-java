package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.entity.Client;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AwardedMilesTest {

    @Test
    void canGetMilesAmount() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);

        // when
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // then
        assertThat(awardedMiles.getMilesAmount(Instant.now())).isEqualTo(10);
    }

    @Test
    void canGetExpirationDate() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);

        // when
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // then
        assertThat(awardedMiles.getExpirationDate()).isEqualTo(Instant.MAX);
    }

    @Test
    void canGetIsNotExpired() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);

        // when
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // then
        assertThat(awardedMiles.isNotExpired(Instant.now())).isTrue();
    }

    @Test
    void canGetCantExpire() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);

        // when
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // then
        assertThat(awardedMiles.cantExpire()).isTrue();
    }

    @Test
    void canSubtractMiles() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);
        // and
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // when
        awardedMiles.subtract(5, Instant.now());

        // then
        assertThat(awardedMiles.getMiles()).isEqualTo(ConstantUntil.constantUntilForever(5));
    }

    @Test
    void canRemoveAllMiles() {
        // given
        AwardsAccount awardsAccount = AwardsAccount.notActiveAccount(null, null);
        Miles miles = ConstantUntil.constantUntilForever(10);
        // and
        AwardedMiles awardedMiles = new AwardedMiles(null, null, miles, awardsAccount);

        // when
        awardedMiles.removeAllMiles(Instant.now());

        // then
        assertThat(awardedMiles.getMiles()).isEqualTo(ConstantUntil.constantUntilForever(0));
    }

    @Test
    void canTransferToAccount() {
        // given
        Client clientFrom = new Client();
        AwardsAccount awardsAccountFrom = AwardsAccount.notActiveAccount(clientFrom, null);
        Client clientTo = new Client();
        AwardsAccount awardsAccountTo = AwardsAccount.notActiveAccount(clientTo, null);
        // and
        AwardedMiles awardedMiles = new AwardedMiles(null, null, null, awardsAccountFrom);

        // when
        awardedMiles.transferToAccount(awardsAccountTo);

        // then
        assertThat(awardedMiles.getClient()).isEqualTo(clientTo);
        assertThat(awardedMiles.getAccount()).isEqualTo(awardsAccountTo);
    }
}
