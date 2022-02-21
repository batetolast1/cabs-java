package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CalculateTransitDistanceTest {

    @Test
    void shouldNotWorkWithInvalidUnit() {
        // given
        TransitDTO transitDTO = transitForDistance(10);
        // and
        String invalidUnit = "invalid unit";

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> transitDTO.getDistance(invalidUnit));
    }

    @Test
    void shouldRepresentAsKm() {
        assertThat(transitForDistance(10).getDistance("km")).isEqualTo("10km");
        assertThat(transitForDistance(10.123f).getDistance("km")).isEqualTo("10.123km");
        assertThat(transitForDistance(10.123678f).getDistance("km")).isEqualTo("10.124km");
        assertThat(transitForDistance(0).getDistance("km")).isEqualTo("0km");
    }

    @Test
    void shouldRepresentAsMeters() {
        assertThat(transitForDistance(10).getDistance("m")).isEqualTo("10000m");
        assertThat(transitForDistance(10.123f).getDistance("m")).isEqualTo("10123m");
        assertThat(transitForDistance(10.123678f).getDistance("m")).isEqualTo("10124m");
        assertThat(transitForDistance(0).getDistance("m")).isEqualTo("0m");
    }

    @Test
    void shouldRepresentAsMiles() {
        assertThat(transitForDistance(10).getDistance("miles")).isEqualTo("6.214miles");
        assertThat(transitForDistance(10.123f).getDistance("miles")).isEqualTo("6.290miles");
        assertThat(transitForDistance(10.123678f).getDistance("miles")).isEqualTo("6.291miles");
        assertThat(transitForDistance(0).getDistance("miles")).isEqualTo("0miles");
    }

    private TransitDTO transitForDistance(float km) {
        Transit t = new Transit();
        t.setPrice(new Money(10));
        t.setDateTime(Instant.now());
        t.setTo(new Address());
        t.setFrom(new Address());
        t.setStatus(Transit.Status.DRAFT);
        t.setKm(Distance.ofKm(km));
        t.setClient(new Client());
        return new TransitDTO(t);
    }
}