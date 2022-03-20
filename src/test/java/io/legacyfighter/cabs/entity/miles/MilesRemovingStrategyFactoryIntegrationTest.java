package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest
class MilesRemovingStrategyFactoryIntegrationTest {

    @Autowired
    private Fixtures fixtures;

    @MockBean
    private AppProperties appProperties;

    @Autowired
    private MilesRemovingStrategyFactory milesRemovingStrategyFactory;

    @BeforeEach
    void setUp() {
        when(appProperties.getAutomaticRefundForVipThreshold()).thenReturn(10);
        when(appProperties.getNoOfTransitsForClaimAutomaticRefund()).thenReturn(3);
    }

    @Test
    void shouldReturnStrategyForClient() {
        // when
        Client client = fixtures.aClientWithClaims(Client.Type.NORMAL, 4);

        // when
        AwardedMilesRemoveStrategy strategy = milesRemovingStrategyFactory.chooseFor(client);

        // then
        assertThat(strategy).isInstanceOf(TooManyClaimsRemoveStrategy.class);
    }

    @Test
    void shouldThrowExceptionWhenClientIsNull() {
        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> milesRemovingStrategyFactory.chooseFor(null));
    }
}
