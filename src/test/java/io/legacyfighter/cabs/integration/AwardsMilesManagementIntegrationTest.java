package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.AwardedMiles;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.AwardedMilesRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class AwardsMilesManagementIntegrationTest {

    private static final Instant _2022_03_14_12_00_00 = LocalDateTime.of(2022, 3, 14, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());

    @MockBean
    private Clock clock;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AwardedMilesRepository awardedMilesRepository;

    @Autowired
    private AwardsService awardsService;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(_2022_03_14_12_00_00);
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
        assertThat(awardsAccount.getDate()).isEqualTo(_2022_03_14_12_00_00);
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThat(awardedMiles).hasSize(1);
        assertThat(awardedMiles.get(0).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(awardedMiles.get(0).getClient()).isEqualTo(client);
        assertThat(awardedMiles.get(0).getMiles()).isEqualTo(10);
        assertThat(awardedMiles.get(0).getExpirationDate()).isEqualTo(_2022_03_14_12_00_00.plus(365, ChronoUnit.DAYS));
        assertThat(awardedMiles.get(0).cantExpire()).isFalse();
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThat(awardedMiles).hasSize(3);
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(differentClient);
        assertThat(awardedMiles).hasSize(1);
        assertThat(awardedMiles.get(0).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(awardedMiles.get(0).getClient()).isEqualTo(differentClient);
        assertThat(awardedMiles.get(0).getMiles()).isEqualTo(10);
        assertThat(awardedMiles.get(0).getExpirationDate()).isEqualTo(_2022_03_14_12_00_00.plus(365, ChronoUnit.DAYS));
        assertThat(awardedMiles.get(0).cantExpire()).isFalse();
    }

    @Test
    void cantRegisterMilesForInactiveAccount() {
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThat(awardedMiles).isEmpty();
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThat(awardedMiles).hasSize(1);
        assertThat(awardedMiles.get(0).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(awardedMiles.get(0).getClient()).isEqualTo(client);
        assertThat(awardedMiles.get(0).getMiles()).isEqualTo(10);
        assertThat(awardedMiles.get(0).getExpirationDate()).isNull();
        assertThat(awardedMiles.get(0).cantExpire()).isTrue();
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

        List<AwardedMiles> awardedMiles = awardedMilesRepository.findAllByClient(client);
        assertThat(awardedMiles).hasSize(1);
        assertThat(awardedMiles.get(0).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(awardedMiles.get(0).getClient()).isEqualTo(client);
        assertThat(awardedMiles.get(0).getMiles()).isEqualTo(10);
        assertThat(awardedMiles.get(0).getExpirationDate()).isNull();
        assertThat(awardedMiles.get(0).cantExpire()).isTrue();
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
        Integer miles = awardsService.calculateBalance(client.getId());

        // then
        assertThat(miles).isEqualTo(30);

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
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 20);
        awardsService.transferMiles(clientFrom.getId(), clientTo.getId(), 30);

        // then
        AwardsAccountDTO awardsAccountFrom = awardsService.findBy(clientFrom.getId());
        assertThat(awardsAccountFrom.getClient().getId()).isEqualTo(clientFrom.getId());
        assertThat(awardsAccountFrom.getTransactions()).isEqualTo(3);
        Integer clientFromBalance = awardsService.calculateBalance(clientFrom.getId());
        assertThat(clientFromBalance).isZero();
        List<AwardedMiles> milesByClientFrom = awardedMilesRepository.findAllByClient(clientFrom);
        assertThat(milesByClientFrom).isEmpty();

        AwardsAccountDTO awardsAccountTo = awardsService.findBy(clientTo.getId());
        assertThat(awardsAccountTo.getClient().getId()).isEqualTo(clientTo.getId());
        assertThat(awardsAccountTo.getTransactions()).isOne();
        Integer clientToBalance = awardsService.calculateBalance(clientTo.getId());
        assertThat(clientToBalance).isEqualTo(30);
        List<AwardedMiles> milesByClientTo = awardedMilesRepository.findAllByClient(clientTo);
        assertThat(milesByClientTo).hasSize(2);
        assertThat(milesByClientTo.get(0).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(milesByClientTo.get(0).getClient()).isEqualTo(clientTo);
        assertThat(milesByClientTo.get(0).getMiles()).isEqualTo(10);
        assertThat(milesByClientTo.get(0).getExpirationDate()).isEqualTo(_2022_03_14_12_00_00.plus(365, ChronoUnit.DAYS));
        assertThat(milesByClientTo.get(0).cantExpire()).isFalse();
        assertThat(milesByClientTo.get(1).getDate()).isEqualTo(_2022_03_14_12_00_00);
        assertThat(milesByClientTo.get(1).getClient()).isEqualTo(clientTo);
        assertThat(milesByClientTo.get(1).getMiles()).isEqualTo(20);
        assertThat(milesByClientTo.get(1).getExpirationDate()).isNull();
        assertThat(milesByClientTo.get(1).cantExpire()).isTrue();
    }

    @Test
    void cannotTransferMilesIfClientFromIsNotActive() {
        // when
        Client clientFrom = fixtures.aClient();
        Client clientTo = fixtures.aClient();
        // and
        fixtures.hasRegisteredAwardsAccount(clientFrom);
        fixtures.hasActiveAwardsAccount(clientTo);

        // when
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 20);
        awardsService.transferMiles(clientFrom.getId(), clientTo.getId(), 20);

        // then
        AwardsAccountDTO awardsAccountFrom = awardsService.findBy(clientFrom.getId());
        assertThat(awardsAccountFrom.getClient().getId()).isEqualTo(clientFrom.getId());
        assertThat(awardsAccountFrom.getTransactions()).isEqualTo(1);
        Integer clientFromBalance = awardsService.calculateBalance(clientFrom.getId());
        assertThat(clientFromBalance).isEqualTo(20);

        AwardsAccountDTO awardsAccountTo = awardsService.findBy(clientTo.getId());
        assertThat(awardsAccountTo.getClient().getId()).isEqualTo(clientTo.getId());
        assertThat(awardsAccountTo.getTransactions()).isZero();
        Integer clientToBalance = awardsService.calculateBalance(clientTo.getId());
        assertThat(clientToBalance).isZero();
    }

    @Test
    void cannotTransferMilesIfClientFromHasNotEnough() {
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
        awardsService.registerNonExpiringMiles(clientFrom.getId(), 20);
        awardsService.transferMiles(clientFrom.getId(), clientTo.getId(), 40);

        // then
        AwardsAccountDTO awardsAccountFrom = awardsService.findBy(clientFrom.getId());
        assertThat(awardsAccountFrom.getClient().getId()).isEqualTo(clientFrom.getId());
        assertThat(awardsAccountFrom.getTransactions()).isEqualTo(2);
        Integer clientFromBalance = awardsService.calculateBalance(clientFrom.getId());
        assertThat(clientFromBalance).isEqualTo(30);

        AwardsAccountDTO awardsAccountTo = awardsService.findBy(clientTo.getId());
        assertThat(awardsAccountTo.getClient().getId()).isEqualTo(clientTo.getId());
        assertThat(awardsAccountTo.getTransactions()).isZero();
        Integer clientToBalance = awardsService.calculateBalance(clientTo.getId());
        assertThat(clientToBalance).isZero();
    }
}