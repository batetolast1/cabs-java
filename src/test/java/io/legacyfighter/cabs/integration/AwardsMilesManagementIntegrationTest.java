package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.service.AwardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static io.legacyfighter.cabs.entity.Client.Type.NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
class AwardsMilesManagementIntegrationTest {

    private static final Instant NOW = LocalDateTime.of(2022, 3, 14, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());

    @MockBean
    private Clock clock;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AwardsService awardsService;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW);
    }

    @Test
    void canRegisterToProgram() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);

        // when
        awardsService.registerToProgram(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isFalse();
        assertThat(awardsAccount.getDate()).isEqualTo(NOW);
        assertThat(awardsAccount.getTransactions()).isZero();
    }

    @Test
    void canActivateAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasRegisteredAwardsAccount(client);

        // when
        awardsService.activateAccount(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isTrue();
    }

    @Test
    void canActivateActivatedAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.activateAccount(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isTrue();
    }

    @Test
    void canDeactivateRegisteredAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasRegisteredAwardsAccount(client);

        // when
        awardsService.deactivateAccount(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isFalse();
    }

    @Test
    void canDeactivateActivatedAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.deactivateAccount(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isFalse();
    }

    @Test
    void canDeactivateDeactivatedAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasInactiveAwardsAccount(client);

        // when
        awardsService.deactivateAccount(client.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.isActive()).isFalse();
    }

    @Test
    void canRegisterMiles() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.registerMiles(client.getId(), transit.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(1);

        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(10);
    }

    @Test
    void canRegisterMilesMultipleTimesForTheSameTransit() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.registerMiles(client.getId(), transit.getId());
        awardsService.registerMiles(client.getId(), transit.getId());
        awardsService.registerMiles(client.getId(), transit.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(3);

        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(30);
    }

    @Test
    void canRegisterMilesForDifferentClient() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        Client differentClient = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);
        // and
        fixtures.hasActiveAwardsAccount(differentClient);

        // when
        awardsService.registerMiles(differentClient.getId(), transit.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(differentClient.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(differentClient.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(1);
    }

    @Test
    void cannotRegisterMilesForInactiveAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);
        // and
        fixtures.hasRegisteredAwardsAccount(client);

        // when
        awardsService.registerMiles(client.getId(), transit.getId());

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isZero();

        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isZero();
    }

    @Test
    void canRegisterNonExpiringMiles() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.registerNonExpiringMiles(client.getId(), 10);

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(1);

        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(10);
    }

    @Test
    void canRegisterNonExpiringMilesForInactiveAccount() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        fixtures.hasRegisteredAwardsAccount(client);

        // when
        awardsService.registerNonExpiringMiles(client.getId(), 10);

        // then
        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(1);

        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(10);
    }

    @Test
    void canCalculateMilesBalance() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);
        // and
        fixtures.hasActiveAwardsAccount(client);

        // when
        awardsService.registerMiles(client.getId(), transit.getId());
        awardsService.registerNonExpiringMiles(client.getId(), 20);

        // then
        Integer balance = awardsService.calculateBalance(client.getId());
        assertThat(balance).isEqualTo(30);

        AwardsAccountDTO awardsAccount = awardsService.findBy(client.getId());
        assertThat(awardsAccount.getClient().getId()).isEqualTo(client.getId());
        assertThat(awardsAccount.getTransactions()).isEqualTo(2);
    }

    @Test
    void canTransferMiles() {
        // when
        Client clientFrom = fixtures.aClient();
        Client clientTo = fixtures.aClient();
        // and
        fixtures.hasActiveAwardsAccount(clientFrom);
        fixtures.hasActiveAwardsAccount(clientTo);
        // and
        Transit transit = fixtures.aCompletedTransitFor(clientFrom);

        // when
        awardsService.registerMiles(clientFrom.getId(), transit.getId());
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 15);
        awardsService.registerMiles(clientFrom.getId(), transit.getId());
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 20);

        awardsService.transferMiles(clientFrom.getId(), clientTo.getId(), 27);

        // then
        AwardsAccountDTO awardsAccountFrom = awardsService.findBy(clientFrom.getId());
        assertThat(awardsAccountFrom.getClient().getId()).isEqualTo(clientFrom.getId());
        assertThat(awardsAccountFrom.getTransactions()).isEqualTo(5);

        Integer clientFromBalance = awardsService.calculateBalance(clientFrom.getId());
        assertThat(clientFromBalance).isEqualTo(28);

        // and
        AwardsAccountDTO awardsAccountTo = awardsService.findBy(clientTo.getId());
        assertThat(awardsAccountTo.getClient().getId()).isEqualTo(clientTo.getId());
        assertThat(awardsAccountTo.getTransactions()).isEqualTo(1);

        Integer clientToBalance = awardsService.calculateBalance(clientTo.getId());
        assertThat(clientToBalance).isEqualTo(27);
    }

    @Test
    void cannotTransferMilesIfClientFromIsNotActive() {
        // when
        Client clientFrom = fixtures.aClient();
        Long clientFromId = clientFrom.getId();
        Client clientTo = fixtures.aClient();
        Long clientToId = clientTo.getId();
        // and
        fixtures.hasRegisteredAwardsAccount(clientFrom);
        fixtures.hasActiveAwardsAccount(clientTo);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsService.transferMiles(clientFromId, clientToId, 20));
    }

    @Test
    void cannotTransferMilesIfClientFromHasNotEnough() {
        // when
        Client clientFrom = fixtures.aClient();
        Long clientFromId = clientFrom.getId();
        Client clientTo = fixtures.aClient();
        Long clientToId = clientTo.getId();
        // and
        fixtures.hasActiveAwardsAccount(clientFrom);
        fixtures.hasActiveAwardsAccount(clientTo);
        // and
        Transit transit = fixtures.aCompletedTransitFor(clientFrom);
        awardsService.registerMiles(clientFrom.getId(), transit.getId());
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 20);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsService.transferMiles(clientFromId, clientToId, 40));
    }

    @Test
    void cannotRemoveMilesWhenAccountIsNotActive() {
        // given
        Client client = fixtures.aClient(NORMAL);
        Long clientId = client.getId();
        // and
        fixtures.hasRegisteredAwardsAccount(client);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsService.removeMiles(clientId, 40));
    }

    @Test
    void cannotRemoveMilesWhenNotEnoughMiles() {
        // given
        Client client = fixtures.aClient(NORMAL);
        Long clientId = client.getId();
        // and
        fixtures.hasRegisteredAwardsAccount(client);
        // and
        awardsService.registerNonExpiringMiles(clientId, 20);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> awardsService.removeMiles(clientId, 40));
    }
}
