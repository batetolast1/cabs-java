package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.entity.Claim;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.service.AwardsService;
import io.legacyfighter.cabs.service.ClaimService;
import io.legacyfighter.cabs.service.ClientNotificationService;
import io.legacyfighter.cabs.service.DriverNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @deprecated use {@link io.legacyfighter.cabs.entity.ClaimsAutomaticResolvingTest}
 */
@SpringBootTest
class ClaimAutomaticResolvingIntegrationTest {

    @Autowired
    private ClaimService claimService;

    @MockBean
    ClientNotificationService clientNotificationService;

    @MockBean
    DriverNotificationService driverNotificationService;

    @MockBean
    AwardsService awardsService;

    @MockBean
    AppProperties appProperties;

    @Autowired
    Fixtures fixtures;

    @Test
    void secondClaimForTheSameTransitWillBeEscalated() {
        // when
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClient(Client.Type.VIP);
        // and
        Transit transit = fixtures.aCompletedTransitFor(driver, client, 39);
        // and
        Claim firstClaim = fixtures.createClaim(client, transit);
        // and
        Claim firstClaimResult = claimService.tryToResolveAutomatically(firstClaim.getId());
        // and
        Claim secondClaim = fixtures.createClaim(client, transit);
        // and
        clearInvocations(clientNotificationService);

        // when
        Claim secondClaimResult = claimService.tryToResolveAutomatically(secondClaim.getId());

        // then
        assertThat(firstClaimResult.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(firstClaimResult.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);

        assertThat(secondClaimResult.getStatus()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(secondClaimResult.getCompletionMode()).isEqualTo(Claim.CompletionMode.MANUAL);

        verifyNoInteractions(clientNotificationService, awardsService, driverNotificationService);
    }

    @Test
    void onlyFirstThreeClaimsAreRefundedForNormalClientWhenFewTransits() {
        // when
        transitCostForAutomaticRefundThresholdIs(40);
        // and
        noOfTransitsForAutomaticRefundIs(10);
        // and
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit1 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit2 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit3 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit4 = fixtures.aCompletedTransitFor(driver, client, 39);

        // and
        Claim claim1 = fixtures.createClaim(client, transit1);
        Claim claimResult1 = claimService.tryToResolveAutomatically(claim1.getId());
        Claim claim2 = fixtures.createClaim(client, transit2);
        Claim claimResult2 = claimService.tryToResolveAutomatically(claim2.getId());
        Claim claim3 = fixtures.createClaim(client, transit3);
        Claim claimResult3 = claimService.tryToResolveAutomatically(claim3.getId());
        Claim claim4 = fixtures.createClaim(client, transit4);

        // when
        Claim claimResult4 = claimService.tryToResolveAutomatically(claim4.getId());

        // then
        assertThat(claimResult1.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim1.getClaimNo(), client.getId());

        assertThat(claimResult2.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim2.getClaimNo(), client.getId());

        assertThat(claimResult3.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim3.getClaimNo(), client.getId());

        assertThat(claimResult4.getStatus()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult4.getCompletionMode()).isEqualTo(Claim.CompletionMode.MANUAL);
        verify(driverNotificationService, times(1)).askDriverForDetailsAboutClaim(claim4.getClaimNo(), driver.getId());

        verifyNoInteractions(awardsService);
        verifyNoMoreInteractions(clientNotificationService, driverNotificationService);
    }

    @Test
    void moreThanThreeLowCostTransitsAreRefundedIfClientIsVIP() {
        // when
        transitCostForAutomaticRefundThresholdIs(40);
        // and
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClient(Client.Type.VIP);
        // and
        Transit transit1 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit2 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit3 = fixtures.aCompletedTransitFor(driver, client, 39);
        Transit transit4 = fixtures.aCompletedTransitFor(driver, client, 39);

        // and
        Claim claim1 = fixtures.createClaim(client, transit1);
        Claim claimResult1 = claimService.tryToResolveAutomatically(claim1.getId());
        Claim claim2 = fixtures.createClaim(client, transit2);
        Claim claimResult2 = claimService.tryToResolveAutomatically(claim2.getId());
        Claim claim3 = fixtures.createClaim(client, transit3);
        Claim claimResult3 = claimService.tryToResolveAutomatically(claim3.getId());
        Claim claim4 = fixtures.createClaim(client, transit4);

        // when
        Claim claimResult4 = claimService.tryToResolveAutomatically(claim4.getId());

        // then
        assertThat(claimResult1.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult1.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim1.getClaimNo(), client.getId());

        assertThat(claimResult2.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult2.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim2.getClaimNo(), client.getId());

        assertThat(claimResult3.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult3.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim3.getClaimNo(), client.getId());

        assertThat(claimResult4.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult4.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claim4.getClaimNo(), client.getId());

        verify(awardsService, times(1)).registerSpecialMiles(client.getId(), 10);
        verifyNoInteractions(driverNotificationService);
        verifyNoMoreInteractions(clientNotificationService, awardsService);
    }

    @Test
    void highCostTransitsAreEscalatedEvenWhenClientIsVIP() {
        // when
        transitCostForAutomaticRefundThresholdIs(40);
        // and
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClientWithClaims(Client.Type.VIP, 3);
        // and
        Transit transit = fixtures.aCompletedTransitFor(driver, client, 41);
        // and
        Claim claim = fixtures.createClaim(client, transit);
        // and
        clearInvocations(awardsService, clientNotificationService);

        // when
        Claim claimResult = claimService.tryToResolveAutomatically(claim.getId());

        // then
        assertThat(claimResult.getStatus()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult.getCompletionMode()).isEqualTo(Claim.CompletionMode.MANUAL);
        verify(driverNotificationService, times(1)).askDriverForDetailsAboutClaim(claim.getClaimNo(), driver.getId());
        verifyNoInteractions(awardsService, clientNotificationService);
        verifyNoMoreInteractions(driverNotificationService);
    }

    @Test
    void lowCostTransitsAreRefundedForNormalClientWhenManyTransits() {
        // when
        transitCostForAutomaticRefundThresholdIs(40);
        // and
        noOfTransitsForAutomaticRefundIs(5);
        // and
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClientWithClaims(Client.Type.NORMAL, 3);
        // and
        fixtures.clientHasDoneTransits(client, 2);
        // and
        Transit transit = fixtures.aCompletedTransitFor(driver, client, 39);
        // and
        Claim claim = fixtures.createClaim(client, transit);
        // and
        clearInvocations(clientNotificationService);

        // when
        Claim claimResult = claimService.tryToResolveAutomatically(claim.getId());

        // then
        assertThat(claimResult.getStatus()).isEqualTo(Claim.Status.REFUNDED);
        assertThat(claimResult.getCompletionMode()).isEqualTo(Claim.CompletionMode.AUTOMATIC);
        verify(clientNotificationService, times(1)).notifyClientAboutRefund(claimResult.getClaimNo(), client.getId());
        verifyNoInteractions(awardsService, driverNotificationService);
        verifyNoMoreInteractions(clientNotificationService);
    }

    @Test
    void highCostTransitsAreEscalatedForNormalClientEvenWithManyTransits() {
        // when
        transitCostForAutomaticRefundThresholdIs(50);
        // and
        noOfTransitsForAutomaticRefundIs(5);
        // and
        Driver driver = fixtures.aDriver();
        // and
        Client client = fixtures.aClientWithClaims(Client.Type.NORMAL, 3);
        // and
        fixtures.clientHasDoneTransits(client, 2);
        // and
        Transit transit = fixtures.aCompletedTransitFor(driver, client, 50);
        // and
        Claim claim = fixtures.createClaim(client, transit);
        // and
        clearInvocations(clientNotificationService);

        // when
        Claim claimResult = claimService.tryToResolveAutomatically(claim.getId());

        // then
        assertThat(claimResult.getStatus()).isEqualTo(Claim.Status.ESCALATED);
        assertThat(claimResult.getCompletionMode()).isEqualTo(Claim.CompletionMode.MANUAL);
        verify(clientNotificationService, times(1)).askForMoreInformation(claimResult.getClaimNo(), client.getId());
        verifyNoInteractions(awardsService, driverNotificationService);
        verifyNoMoreInteractions(clientNotificationService);
    }

    void transitCostForAutomaticRefundThresholdIs(int price) {
        when(appProperties.getAutomaticRefundForVipThreshold()).thenReturn(price);
    }

    void noOfTransitsForAutomaticRefundIs(int no) {
        when(appProperties.getNoOfTransitsForClaimAutomaticRefund()).thenReturn(no);
    }
}