package io.legacyfighter.cabs.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MilesTest {

    private final static Instant YESTERDAY = Instant.now().minus(1, ChronoUnit.DAYS);
    private final static Instant TODAY = Instant.now();
    private final static Instant TOMORROW = Instant.now().plus(1, ChronoUnit.DAYS);

    @Test
    void milesWithoutExpirationDateDontExpire() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntilForever(10);

        // then
        assertThat(miles.expiresAt()).isEqualTo(Instant.MAX);
        assertThat(miles.getAmountFor(YESTERDAY)).isEqualTo(10);
        assertThat(miles.getAmountFor(TODAY)).isEqualTo(10);
        assertThat(miles.getAmountFor(TOMORROW)).isEqualTo(10);
    }

    @Test
    void expiringMilesExpire() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntil(10, TODAY);

        // then
        assertThat(miles.expiresAt()).isEqualTo(TODAY);
        assertThat(miles.getAmountFor(YESTERDAY)).isEqualTo(10);
        assertThat(miles.getAmountFor(TODAY)).isEqualTo(10);
        assertThat(miles.getAmountFor(TOMORROW)).isZero();
    }

    @Test
    void canSubtractWhenEnoughMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntilForever(10);

        // when
        Miles result = miles.subtract(5, TODAY);

        // then
        assertThat(result.getAmountFor(TODAY)).isEqualTo(5);
        assertThat(result.expiresAt()).isEqualTo(Instant.MAX);
    }

    @Test
    void canSubtractZeroMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntilForever(10);

        // when
        Miles result = miles.subtract(0, TODAY);

        // then
        assertThat(result.getAmountFor(TODAY)).isEqualTo(10);
        assertThat(result.expiresAt()).isEqualTo(Instant.MAX);
    }

    @Test
    void canSubtractWhenEnoughExpiringMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntil(10, TODAY);

        // when
        Miles result = miles.subtract(5, TODAY);

        // then
        assertThat(result.getAmountFor(TODAY)).isEqualTo(5);
        assertThat(result.expiresAt()).isEqualTo(TODAY);
    }

    @Test
    void canSubtractZeroExpiringMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntil(10, TODAY);

        // when
        Miles result = miles.subtract(0, TODAY);

        // then
        assertThat(result.getAmountFor(TODAY)).isEqualTo(10);
        assertThat(result.expiresAt()).isEqualTo(TODAY);
    }

    @Test
    void cannotSubtractNegativeMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntilForever(10);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> miles.subtract(-15, TODAY));
    }

    @Test
    void cannotSubtractWhenNotEnoughMiles() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntilForever(10);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> miles.subtract(15, TODAY));
    }

    @Test
    void cannotSubtractWhenMilesExpired() {
        // given
        ConstantUntil miles = ConstantUntil.constantUntil(10, TODAY);

        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> miles.subtract(15, TOMORROW));
    }
}