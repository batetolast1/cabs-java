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
        Claim firstClaim = createClaim(transit);
        Claim secondClaim = createClaim(transit);

        // when
        ClaimsResolver.Result firstClaimResult = claimsResolver.resolve(firstClaim, 40, 15, 10);
        ClaimsResolver.Result secondClaimResult = claimsResolver.resolve(secondClaim, 40, 15, 10);

        // then
        assertThat(firstClaimResult.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(firstClaimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(secondClaimResult.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(secondClaimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

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
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 40, 3, 10);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 40, 3, 10);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 40, 3, 10);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 40, 3, 10);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_DRIVER);
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
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 40, 15, 10);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 40, 15, 10);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 40, 15, 10);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 40, 15, 10);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
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
        claimsResolver.resolve(claim1, 40, 15, 10);
        claimsResolver.resolve(claim2, 40, 15, 10);
        claimsResolver.resolve(claim3, 40, 15, 10);

        // when
        ClaimsResolver.Result claimResult = claimsResolver.resolve(claim4, 40, 15, 10);

        // then
        assertThat(claimResult.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_DRIVER);
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

        // when
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 40, 3, 3);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 40, 3, 3);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 40, 3, 3);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 40, 3, 3);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);
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

        // when
        ClaimsResolver.Result claimResult1 = claimsResolver.resolve(claim1, 40, 3, 3);
        ClaimsResolver.Result claimResult2 = claimsResolver.resolve(claim2, 40, 3, 3);
        ClaimsResolver.Result claimResult3 = claimsResolver.resolve(claim3, 40, 3, 3);
        ClaimsResolver.Result claimResult4 = claimsResolver.resolve(claim4, 40, 3, 3);

        // then
        assertThat(claimResult1.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult2.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult3.getDecision()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_NO_ONE);

        assertThat(claimResult4.getDecision()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult4.getWhoToAsk()).isEqualTo(ClaimsResolver.WhoToAsk.ASK_CLIENT);
    }
}