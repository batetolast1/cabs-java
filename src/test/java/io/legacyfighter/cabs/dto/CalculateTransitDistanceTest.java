package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.distance.Distance;
import io.legacyfighter.cabs.entity.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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
        Instant dateTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
        Distance distance = Distance.ofKm(km);
        Driver driver = new Driver();
        driver.setDriverLicense(DriverLicense.withoutValidation(""));
        Transit transit = new Transit(new Address(), new Address(), new Client(), CarType.CarClass.VAN, dateTime, distance);
        transit.proposeTo(driver);
        transit.acceptBy(driver, dateTime);
        transit.startAt(dateTime);
        transit.completeAt(dateTime, new Address(), distance);
        return new TransitDTO(transit);
    }
}