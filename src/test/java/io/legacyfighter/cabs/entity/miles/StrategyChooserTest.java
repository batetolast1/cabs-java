package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.entity.Client;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyChooserTest {

    @Test
    void shouldReturnCorrectStrategyWhenNormalClientHasTooManyClaims() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(0, 3, DayOfWeek.MONDAY);
        // and
        Client client = aClient(Client.Type.NORMAL);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(TooManyClaimsRemoveStrategy.class);
    }

    @Test
    void shouldReturnCorrectStrategyWhenVIPClientHasTooManyClaims() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(0, 3, DayOfWeek.MONDAY);
        // and
        Client client = aClient(Client.Type.VIP);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(TooManyClaimsRemoveStrategy.class);
    }

    @Test
    void shouldReturnCorrectStrategyWhenVIPClient() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(0, 2, DayOfWeek.MONDAY);
        // and
        Client client = aClient(Client.Type.VIP);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(VipClientRemoveStrategy.class);
    }

    @Test
    void shouldReturnCorrectStrategyWhenNormalClientHasManyTransitsAndItsSunday() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(15, 2, DayOfWeek.SUNDAY);
        // and
        Client client = aClient(Client.Type.NORMAL);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(VipClientRemoveStrategy.class);
    }

    @Test
    void shouldReturnCorrectStrategyWhenNormalClientHasManyTransitsAndItsNotSunday() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(15, 2, DayOfWeek.MONDAY);
        // and
        Client client = aClient(Client.Type.NORMAL);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(NormalClientRemoveStrategy.class);
    }

    @Test
    void shouldReturnCorrectStrategyWhenNormalClientHasFewTransits() {
        // given
        StrategyChooser strategyChooser = new StrategyChooser(14, 2, DayOfWeek.MONDAY);
        // and
        Client client = aClient(Client.Type.NORMAL);

        // when
        AwardedMilesRemoveStrategy strategy = strategyChooser.chooseFor(client);

        // then
        assertThat(strategy).isExactlyInstanceOf(DefaultRemoveStrategy.class);
    }

    private Client aClient(Client.Type clientType) {
        Client client = new Client();
        client.setType(clientType);
        return client;
    }
}
