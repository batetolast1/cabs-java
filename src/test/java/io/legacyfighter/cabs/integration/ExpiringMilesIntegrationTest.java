package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class ExpiringMilesIntegrationTest {

    private static final Instant _2022_03_14_12_00_00 = LocalDateTime.of(2022, 3, 14, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant _2022_03_15_12_00_00 = LocalDateTime.of(2022, 3, 15, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant _2022_03_16_12_00_00 = LocalDateTime.of(2022, 3, 16, 12, 0, 0).toInstant(OffsetDateTime.now().getOffset());

    @MockBean
    private Clock clock;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AwardsService awardsService;

    @Test
    void shouldTakeIntoAccountExpiredMilesWhenCalculatingBalance() {
        // given
        Client client = fixtures.aClient(Client.Type.NORMAL);
        // and
        when(clock.instant()).thenReturn(_2022_03_14_12_00_00);
        // and
        fixtures.hasActiveAwardsAccount(client);
        // and
        Transit transit = fixtures.aCompletedTransitFor(client);

        // when
        registerMilesAt(client, transit, _2022_03_14_12_00_00);
        // then
        assertThat(calculateBalanceAt(client, _2022_03_14_12_00_00)).isEqualTo(10);

        // when
        registerMilesAt(client, transit, _2022_03_15_12_00_00);
        // then
        assertThat(calculateBalanceAt(client, _2022_03_15_12_00_00)).isEqualTo(20);

        // when
        registerMilesAt(client, transit, _2022_03_16_12_00_00);
        // then
        assertThat(calculateBalanceAt(client, _2022_03_16_12_00_00)).isEqualTo(30);

        assertThat(calculateBalanceAt(client, _2022_03_14_12_00_00.plus(364, ChronoUnit.DAYS))).isEqualTo(30);
        assertThat(calculateBalanceAt(client, _2022_03_14_12_00_00.plus(365, ChronoUnit.DAYS))).isEqualTo(20);
        assertThat(calculateBalanceAt(client, _2022_03_15_12_00_00.plus(364, ChronoUnit.DAYS))).isEqualTo(20);
        assertThat(calculateBalanceAt(client, _2022_03_15_12_00_00.plus(365, ChronoUnit.DAYS))).isEqualTo(10);
        assertThat(calculateBalanceAt(client, _2022_03_16_12_00_00.plus(364, ChronoUnit.DAYS))).isEqualTo(10);
        assertThat(calculateBalanceAt(client, _2022_03_16_12_00_00.plus(365, ChronoUnit.DAYS))).isZero();
    }

    private void registerMilesAt(Client client, Transit transit, Instant instant) {
        when(clock.instant()).thenReturn(instant);
        awardsService.registerMiles(client.getId(), transit.getId());
    }

    private int calculateBalanceAt(Client client, Instant instant) {
        when(clock.instant()).thenReturn(instant);
        return awardsService.calculateBalance(client.getId());
    }
}