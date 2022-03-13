package io.legacyfighter.cabs.entity;

import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimsAutomaticResolvingTest {

    @Test
    void secondClaimForTheSameTransitWillBeEscalated() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit = aTransit(1L, 39);
        // and
        Claim claim1 = createClaim(transit);
        Claim claim2 = createClaim(transit);

        // when
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 15, 40, 10);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 15, 40, 10);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult1.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult2.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);
    }

    @Test
    void onlyFirstThreeClaimsAreRefundedForNormalClientWhenFewTransits() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit1 = aTransit(1L, 39);
        Transit transit2 = aTransit(2L, 39);
        Transit transit3 = aTransit(3L, 39);
        Transit transit4 = aTransit(4L, 39);
        // and
        Client client = aClient(Client.Type.NORMAL);
        // and
        Claim claim1 = createClaim(transit1, client);
        Claim claim2 = createClaim(transit2, client);
        Claim claim3 = createClaim(transit3, client);
        Claim claim4 = createClaim(transit4, client);

        // when
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 3, 40, 10);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 3, 40, 10);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 3, 40, 10);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 3, 40, 10);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult1.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult2.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult3.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_DRIVER);
        assertThat(claimResult4.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);
    }

    @Test
    void moreThanThreeLowCostTransitsAreRefundedIfClientIsVIP() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit1 = aTransit(1L, 39);
        Transit transit2 = aTransit(2L, 39);
        Transit transit3 = aTransit(3L, 39);
        Transit transit4 = aTransit(4L, 39);
        // and
        Client client = aClient(Client.Type.VIP);
        // and
        Claim claim1 = createClaim(transit1, client);
        Claim claim2 = createClaim(transit2, client);
        Claim claim3 = createClaim(transit3, client);
        Claim claim4 = createClaim(transit4, client);

        // when
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 15, 40, 10);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 15, 40, 10);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 15, 40, 10);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 15, 40, 10);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult1.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult2.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult3.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult4.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.EXTRA_MILES);
    }

    @Test
    void highCostTransitsAreEscalatedEvenWhenClientIsVIP() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit1 = aTransit(1L, 41);
        Transit transit2 = aTransit(2L, 41);
        Transit transit3 = aTransit(3L, 41);
        Transit transit4 = aTransit(4L, 41);
        // and
        Client client = aClient(Client.Type.VIP);
        // and
        Claim claim1 = createClaim(transit1, client);
        Claim claim2 = createClaim(transit2, client);
        Claim claim3 = createClaim(transit3, client);
        Claim claim4 = createClaim(transit4, client);
        // and
        claimsResolver.resolve(claim1, 15, 40, 10);
        claimsResolver.resolve(claim2, 15, 40, 10);
        claimsResolver.resolve(claim3, 15, 40, 10);

        // when
        ClaimsResolver.Result claimResult = claimsResolver.resolve(claim4, 15, 40, 10);

        // then
        assertThat(claimResult.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_DRIVER);
        assertThat(claimResult.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);
    }

    @Test
    void lowCostTransitsAreRefundedForNormalClientWhenManyTransits() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit1 = aTransit(1L, 39);
        Transit transit2 = aTransit(2L, 39);
        Transit transit3 = aTransit(3L, 39);
        Transit transit4 = aTransit(4L, 39);
        // and
        Client client = aClient(Client.Type.NORMAL);
        // and
        Claim claim1 = createClaim(transit1, client);
        Claim claim2 = createClaim(transit2, client);
        Claim claim3 = createClaim(transit3, client);
        Claim claim4 = createClaim(transit4, client);
        // and
        claimsResolver.resolve(claim1, 3, 40, 3);
        claimsResolver.resolve(claim2, 3, 40, 3);
        claimsResolver.resolve(claim3, 3, 40, 3);

        // when
        ClaimsResolver.Result claimResult = claimsResolver.resolve(claim4, 3, 40, 3);

        // then
        assertThat(claimResult.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
        assertThat(claimResult.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);
    }

    @Test
    void highCostTransitsAreEscalatedForNormalClientEvenWithManyTransits() {
        // given
        ClaimsResolver claimsResolver = new ClaimsResolver();
        // and
        Transit transit1 = aTransit(1L, 41);
        Transit transit2 = aTransit(2L, 41);
        Transit transit3 = aTransit(3L, 41);
        Transit transit4 = aTransit(4L, 41);
        // and
        Client client = aClient(Client.Type.NORMAL);
        // and
        Claim claim1 = createClaim(transit1, client);
        Claim claim2 = createClaim(transit2, client);
        Claim claim3 = createClaim(transit3, client);
        Claim claim4 = createClaim(transit4, client);
        // and
        claimsResolver.resolve(claim1, 3, 40, 3);
        claimsResolver.resolve(claim2, 3, 40, 3);
        claimsResolver.resolve(claim3, 3, 40, 3);

        // when
        ClaimsResolver.Result claimResult = claimsResolver.resolve(claim4, 3, 40, 3);

        // then
        assertThat(claimResult.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_CLIENT);
        assertThat(claimResult.getAwardedMiles()).isEqualTo(ClaimsResolver.AwardedMiles.NO_MILES);
    }

    private Claim createClaim(Transit transit) {
        return createClaim(transit, null);
    }

    private Claim createClaim(Transit transit, Client client) {
        Claim claim = new Claim();
        claim.setTransit(transit);
        claim.setOwner(client);
        return claim;
    }

    private Transit aTransit(long id, int price) {
        Transit transit = new Transit(id);
        transit.setPrice(new Money(price));
        return transit;
    }

    private Client aClient(Client.Type clientType) {
        Client client = new Client();
        client.setType(clientType);
        return client;
    }
}