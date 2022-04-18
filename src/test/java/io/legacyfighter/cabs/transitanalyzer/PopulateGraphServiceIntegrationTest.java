package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.CabsApplication;
import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Driver;
import io.legacyfighter.cabs.repository.TransitRepository;
import io.legacyfighter.cabs.service.GeocodingService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static io.legacyfighter.cabs.entity.CarType.CarClass.VAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CabsApplication.class, PopulateGraphServiceIntegrationTest.TestConfig.class})
class PopulateGraphServiceIntegrationTest {

    private static final Instant NOW = instantOf(8, 11, 0);

    static class TestConfig {

        @Bean
        @Primary
        ApplicationEventPublisher applicationEventPublisher() {
            return new NoOpEventPublisher();
        }
    }

    static class NoOpEventPublisher implements ApplicationEventPublisher {

        @Override
        public void publishEvent(@NotNull Object event) {
            // no-op
        }
    }

    @Autowired
    Fixtures fixtures;

    @Autowired
    TransitRepository transitRepository;

    @Autowired
    GraphTransitAnalyzer graphTransitAnalyzer;

    @MockBean
    GeocodingService geocodingService;

    @MockBean
    Clock clock;

    @Autowired
    PopulateGraphService populateGraphService;

    @BeforeEach
    public void setup() {
        fixtures.anActiveCarCategory(VAN);

        when(clock.instant()).thenReturn(NOW);
        when(geocodingService.geocodeAddress(any(Address.class))).thenReturn(new double[]{1, 1});
    }

    @Test
    void canPopulateGraphWithDataFromRelationalDB() {
        // given
        Client client = fixtures.aClient();
        // and
        Driver driver = fixtures.aNearbyDriver();
        // and
        Address _1 = fixtures.anAddress();
        Address _2 = fixtures.anAddress();
        Address _3 = fixtures.anAddress();
        Address _4 = fixtures.anAddress();
        // and
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 0), instantOf(10, 12, 5), client, driver, _1, _2, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 10), instantOf(10, 12, 15), client, driver, _2, _3, clock);
        fixtures.aRequestedAndCompletedTransit(instantOf(10, 12, 20), instantOf(10, 12, 25), client, driver, _3, _4, clock);

        //when
        populateGraphService.populate();

        //then
        List<Long> result = graphTransitAnalyzer.analyze(client.getId(), _1.getHash());

        assertThat(result).containsExactly(
                _1.getHash().longValue(),
                _2.getHash().longValue(),
                _3.getHash().longValue(),
                _4.getHash().longValue()
        );
    }

    private static Instant instantOf(int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(2022, 4, dayOfMonth, hour, minute, 0).toInstant(OffsetDateTime.now().getOffset());
    }
}
