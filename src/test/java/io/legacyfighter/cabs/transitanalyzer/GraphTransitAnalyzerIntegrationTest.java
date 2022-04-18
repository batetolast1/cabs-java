package io.legacyfighter.cabs.transitanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GraphTransitAnalyzerIntegrationTest {

    @Autowired
    GraphTransitAnalyzer graphTransitAnalyzer;

    @Test
    void canRecognizeNewAddress() {
        //given
        graphTransitAnalyzer.addTransitBetweenAddresses(1L, 1L, 111, 222, Instant.now(), Instant.now());
        graphTransitAnalyzer.addTransitBetweenAddresses(1L, 1L, 222, 333, Instant.now(), Instant.now());
        graphTransitAnalyzer.addTransitBetweenAddresses(1L, 1L, 333, 444, Instant.now(), Instant.now());

        //when
        List<Long> result = graphTransitAnalyzer.analyze(1L, 111);

        //then
        assertThat(result).containsExactly(111L, 222L, 333L, 444L);
    }
}
