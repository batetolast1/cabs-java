package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.AddressDTO;
import io.legacyfighter.cabs.dto.AnalyzedAddressesDTO;
import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.service.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static io.legacyfighter.cabs.entity.CarType.CarClass.VAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AnalyzeNearbyTransitsIntegrationTest {

    private static final Instant NOW = instantOf(8, 11, 0);

    @Autowired
    Fixtures fixtures;

    @MockBean
    GeocodingService geocodingService;

    @MockBean
    Clock clock;

    @Autowired
    TransitAnalyzerController transitAnalyzerController;

    @BeforeEach
    public void setup() {
        fixtures.anActiveCarCategory(VAN);

        when(clock.instant()).thenReturn(NOW);
        when(geocodingService.geocodeAddress(any(Address.class))).thenReturn(new double[]{1, 1});
    }

    @Test
    void canFindLongestTravel() {
        // given
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aNearbyDriver();
        // and
        Address _1 = fixtures.anAddress();
        Address _2 = fixtures.anAddress();
        Address _3 = fixtures.anAddress();
        Address _4 = fixtures.anAddress();
        Address _5 = fixtures.anAddress();
        // and
        // 1-2
        fixtures.aRequestedAndCompletedTransit(instantOf(8, 12, 0), instantOf(8, 12, 5), client, driver, _1, _2, clock);
        // 1-2-3
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 0), instantOf(9, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 10), instantOf(9, 12, 15), client, driver, _2, _3, clock);
        // 1-2-3-4
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 0), instantOf(10, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 10), instantOf(10, 12, 15), client, driver, _2, _3, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 20), instantOf(10, 12, 25), client, driver, _3, _4, clock);
        // 1-2-3-4-5
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 0), instantOf(11, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 10), instantOf(11, 12, 15), client, driver, _2, _3, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 20), instantOf(11, 12, 25), client, driver, _3, _4, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 30), instantOf(11, 12, 35), client, driver, _4, _5, clock);

        //when
        AnalyzedAddressesDTO analyzedAddressesDTO = transitAnalyzerController.analyze(client.getId(), _1.getId());

        // then (1-2-3-4-5)
        assertThat(getHashes(analyzedAddressesDTO)).containsExactly(_1.getHash(), _2.getHash(), _3.getHash(), _4.getHash(), _5.getHash());
    }

    @Test
    void canFindLongestTravelWithLongBreaksBetweenTransits() {
        // given
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aNearbyDriver();
        // and
        Address _1 = fixtures.anAddress();
        Address _2 = fixtures.anAddress();
        Address _3 = fixtures.anAddress();
        Address _4 = fixtures.anAddress();
        Address _5 = fixtures.anAddress();
        // 1-2-3-4-(stop)-5-1
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 0), instantOf(11, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 10), instantOf(11, 12, 15), client, driver, _2, _3, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 20), instantOf(11, 12, 25), client, driver, _3, _4, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 40), instantOf(11, 12, 45), client, driver, _4, _5, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 50), instantOf(11, 12, 55), client, driver, _5, _1, clock);

        //when
        AnalyzedAddressesDTO analyzedAddressesDTO = transitAnalyzerController.analyze(client.getId(), _1.getId());

        // then (1-2-3-4)
        assertThat(getHashes(analyzedAddressesDTO)).containsExactly(_1.getHash(), _2.getHash(), _3.getHash(), _4.getHash());
    }

    @Test
    void canFindLongestTravelWithLoops() {
        // given
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aNearbyDriver();
        // and
        Address _1 = fixtures.anAddress();
        Address _2 = fixtures.anAddress();
        Address _3 = fixtures.anAddress();
        Address _4 = fixtures.anAddress();
        Address _5 = fixtures.anAddress();
        // and
        // 5-1-5
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 0), instantOf(9, 12, 5), client, driver, _5, _1, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 10), instantOf(9, 12, 15), client, driver, _1, _5, clock);
        // 1-5-1-2-3-5-1
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 0), instantOf(10, 12, 5), client, driver, _1, _5, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 10), instantOf(10, 12, 15), client, driver, _5, _1, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 20), instantOf(10, 12, 25), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 30), instantOf(10, 12, 35), client, driver, _2, _3, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 40), instantOf(10, 12, 45), client, driver, _3, _5, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 50), instantOf(10, 12, 55), client, driver, _5, _1, clock);
        // 5-4-5-4-5
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 0), instantOf(11, 12, 5), client, driver, _5, _4, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 10), instantOf(11, 12, 15), client, driver, _4, _5, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 20), instantOf(11, 12, 25), client, driver, _5, _4, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 30), instantOf(11, 12, 35), client, driver, _4, _5, clock);

        //when
        AnalyzedAddressesDTO analyzedAddressesDTO = transitAnalyzerController.analyze(client.getId(), _5.getId());

        // then (5-1-2-3-5-1)
        assertThat(getHashes(analyzedAddressesDTO)).containsExactly(_5.getHash(), _1.getHash(), _2.getHash(), _3.getHash(), _5.getHash(), _1.getHash());
    }

    @Test
    void canFindLongTravelBetweenOthers() {
        // given
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aNearbyDriver();
        // and
        Address _1 = fixtures.anAddress();
        Address _2 = fixtures.anAddress();
        Address _3 = fixtures.anAddress();
        Address _4 = fixtures.anAddress();
        Address _5 = fixtures.anAddress();
        // and
        // 1-2-3
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 0), instantOf(9, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(9, 12, 10), instantOf(9, 12, 15), client, driver, _2, _3, clock);
        // 4-5
        fixtures.aRequestedAndCompletedTransit(instantOf(11, 12, 30), instantOf(11, 12, 35), client, driver, _4, _5, clock);

        //when
        AnalyzedAddressesDTO analyzedAddressesDTO = transitAnalyzerController.analyze(client.getId(), _1.getId());

        // then (1-2-3)
        assertThat(getHashes(analyzedAddressesDTO)).containsExactly(_1.getHash(), _2.getHash(), _3.getHash());
    }

    private static Instant instantOf(int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(2022, 4, dayOfMonth, hour, minute, 0).toInstant(OffsetDateTime.now().getOffset());
    }

    private List<Integer> getHashes(AnalyzedAddressesDTO analyzedAddressesDTO) {
        return analyzedAddressesDTO.getAddresses().stream()
                .map(AddressDTO::getHash)
                .collect(Collectors.toList());
    }
}
