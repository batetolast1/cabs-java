package io.legacyfighter.cabs.distance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void cannotCreateNegativeDistance() {
        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Distance.ofKm(-1));
    }

    @Test
    void canConvertToFloat() {
        assertThat(Distance.ofKm(10).toKmInFloat()).isEqualTo(10);
        assertThat(Distance.ofKm(10.123f).toKmInFloat()).isEqualTo(10.123f);
        assertThat(Distance.ofKm(10.123678f).toKmInFloat()).isEqualTo(10.123678f);
        assertThat(Distance.ZERO.toKmInFloat()).isZero();
    }

    @Test
    void canConvertToDouble() {
        //expect
        assertEquals(10d, Distance.ofKm(10).toKmInDouble());
        assertEquals(10.123d, Distance.ofKm(10.123).toKmInDouble());
        assertEquals(10.123678d, Distance.ofKm(10.123678d).toKmInDouble());
        assertThat(Distance.ZERO.toKmInDouble()).isZero();
    }

    @Test
    void canRepresentDistanceAsMeters() {
        assertThat(Distance.ofKm(10).printIn("m")).isEqualTo("10000m");
        assertThat(Distance.ofKm(10.123f).printIn("m")).isEqualTo("10123m");
        assertThat(Distance.ofKm(10.123678f).printIn("m")).isEqualTo("10124m");
        assertThat(Distance.ZERO.printIn("m")).isEqualTo("0m");
    }

    @Test
    void canRepresentDistanceAsKm() {
        assertThat(Distance.ofKm(10).printIn("km")).isEqualTo("10km");
        assertThat(Distance.ofKm(10.123f).printIn("km")).isEqualTo("10.123km");
        assertThat(Distance.ofKm(10.123678f).printIn("km")).isEqualTo("10.124km");
        assertThat(Distance.ZERO.printIn("km")).isEqualTo("0km");
    }

    @Test
    void canRepresentDistanceAsMiles() {
        assertThat(Distance.ofKm(10).printIn("miles")).isEqualTo("6.214miles");
        assertThat(Distance.ofKm(10.123f).printIn("miles")).isEqualTo("6.290miles");
        assertThat(Distance.ofKm(10.123678f).printIn("miles")).isEqualTo("6.291miles");
        assertThat(Distance.ZERO.printIn("miles")).isEqualTo("0miles");
    }

    @Test
    void canAddDistance() {
        assertThat(Distance.ofKm(10.0).add(Distance.ofKm(11.1))).isEqualTo(Distance.ofKm(21.1));
        assertThat(Distance.ofKm(10.0).add(Distance.ZERO)).isEqualTo(Distance.ofKm(10.0));
        assertThat(Distance.ZERO.add(Distance.ZERO)).isEqualTo(Distance.ZERO);
    }
}
