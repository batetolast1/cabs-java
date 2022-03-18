package io.legacyfighter.cabs.entity.miles;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TwoStepExpiringMilesTest {

    private final static Instant YESTERDAY = Instant.now().minus(1, ChronoUnit.DAYS);
    private final static Instant TODAY = Instant.now();
    private final static Instant TOMORROW = Instant.now().plus(1, ChronoUnit.DAYS);

    @Test
    void twoStepExpiringMilesShouldLeaveHalfOfAmountAfterOneStep() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // then
        assertThat(twoStepExpiringMiles.expiresAt()).isEqualTo(TODAY);
        assertThat(twoStepExpiringMiles.getAmount(YESTERDAY)).isEqualTo(10);
        assertThat(twoStepExpiringMiles.getAmount(TODAY)).isEqualTo(5);
        assertThat(twoStepExpiringMiles.getAmount(TOMORROW)).isZero();
    }

    @Test
    void canSubtractWhenEnoughEvenMiles() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Miles resultBeforeFirstStep = twoStepExpiringMiles.subtract(5, YESTERDAY);
        Miles resultAfterFirstStep = twoStepExpiringMiles.subtract(5, TODAY);

        // then
        Miles expectedResultBeforeFirstStep = new TwoStepExpiringMiles(5, YESTERDAY, TODAY);
        assertThat(resultBeforeFirstStep).isEqualTo(expectedResultBeforeFirstStep);
        Miles expectedResultAfterFirstStep = new TwoStepExpiringMiles(0, YESTERDAY, TODAY);
        assertThat(resultAfterFirstStep).isEqualTo(expectedResultAfterFirstStep);
    }

    @Test
    void canSubtractWhenEnoughOddMiles() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(9, YESTERDAY, TODAY);

        // when
        Miles resultBeforeFirstStep = twoStepExpiringMiles.subtract(4, YESTERDAY);
        Miles resultAfterFirstStep = twoStepExpiringMiles.subtract(4, TODAY);

        // then
        Miles expectedResultBeforeFirstStep = new TwoStepExpiringMiles(5, YESTERDAY, TODAY);
        assertThat(resultBeforeFirstStep).isEqualTo(expectedResultBeforeFirstStep);
        Miles expectedResultAfterFirstStep = new TwoStepExpiringMiles(1, YESTERDAY, TODAY);
        assertThat(resultAfterFirstStep).isEqualTo(expectedResultAfterFirstStep);
    }

    @Test
    void canSubtractZeroMiles() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Miles result = twoStepExpiringMiles.subtract(0, YESTERDAY);

        // then
        Miles expected = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void cannotSubtractNegativeMiles() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> twoStepExpiringMiles.subtract(-15, TODAY));
    }

    @Test
    void cannotSubtractWhenNotEnoughMilesBeforeFirstStep() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> twoStepExpiringMiles.subtract(11, YESTERDAY));
    }

    @Test
    void cannotSubtractWhenNotEnoughMilesAfterFirstStep() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> twoStepExpiringMiles.subtract(6, TODAY));
    }

    @Test
    void cannotSubtractWhenNotEnoughMilesAfterSecondStep() {
        // given
        TwoStepExpiringMiles twoStepExpiringMiles = new TwoStepExpiringMiles(10, YESTERDAY, TODAY);

        // when
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> twoStepExpiringMiles.subtract(1, TOMORROW));
    }
}
