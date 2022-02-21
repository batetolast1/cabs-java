package io.legacyfighter.cabs.distance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DistanceTest {

    @Test
    void cannotUnderstandInvalidUnit() {
        // given
        Distance distance = Distance.ofKm(10);
        // and
        String invalidUnit = "invalid unit";

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> distance.printIn(invalidUnit));
    }

    @Test
    void canConvertToFloat() {
        assertThat(Distance.ofKm(10).toKmInFloat()).isEqualTo(10);
        assertThat(Distance.ofKm(10.123f).toKmInFloat()).isEqualTo(10.123f);
        assertThat(Distance.ofKm(10.123678f).toKmInFloat()).isEqualTo(10.123678f);
        assertThat(Distance.ofKm(0).toKmInFloat()).isZero();
    }

    @Test
    void canRepresentDistanceAsMeters() {
        assertThat(Distance.ofKm(10).printIn("m")).isEqualTo("10000m");
        assertThat(Distance.ofKm(10.123f).printIn("m")).isEqualTo("10123m");
        assertThat(Distance.ofKm(10.123678f).printIn("m")).isEqualTo("10124m");
        assertThat(Distance.ofKm(0).printIn("m")).isEqualTo("0m");
    }

    @Test
    void canRepresentDistanceAsKm() {
        assertThat(Distance.ofKm(10).printIn("km")).isEqualTo("10km");
        assertThat(Distance.ofKm(10.123f).printIn("km")).isEqualTo("10.123km");
        assertThat(Distance.ofKm(10.123678f).printIn("km")).isEqualTo("10.124km");
        assertThat(Distance.ofKm(0).printIn("km")).isEqualTo("0km");
    }

    @Test
    void canRepresentDistanceAsMiles() {
        assertThat(Distance.ofKm(10).printIn("miles")).isEqualTo("6.214miles");
        assertThat(Distance.ofKm(10.123f).printIn("miles")).isEqualTo("6.290miles");
        assertThat(Distance.ofKm(10.123678f).printIn("miles")).isEqualTo("6.291miles");
        assertThat(Distance.ofKm(0).printIn("miles")).isEqualTo("0miles");

    }
}
