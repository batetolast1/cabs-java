package io.legacyfighter.cabs.driverreport.travelleddistance;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TimeSlotTest {

    private static final Instant NOON = LocalDateTime.of(2022, Month.APRIL, 3, 12, 0).toInstant(OffsetDateTime.now().getOffset());
    private static final Instant NOON_FIVE = NOON.plus(5, ChronoUnit.MINUTES);
    private static final Instant NOON_TEN = NOON_FIVE.plus(5, ChronoUnit.MINUTES);

    @Test
    void cannotCreateTimeSlotWhenEndIsBeforeBeginning() {
        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> TimeSlot.of(NOON_FIVE, NOON));
    }

    @Test
    void cannotCreateTimeSlotWhenEndIsEqualToBeginning() {
        // when
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> TimeSlot.of(NOON_FIVE, NOON));
    }

    @Test
    void canCreateTimeSlot() {
        // when
        TimeSlot timeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // then
        assertThat(timeSlot.getBeginning()).isEqualTo(NOON);
        assertThat(timeSlot.getEnd()).isEqualTo(NOON_FIVE);
    }

    @Test
    void canCreatePreviousTimeSlot() {
        // given
        TimeSlot timeSlotToGetPreviousFrom = TimeSlot.of(NOON_FIVE, NOON_TEN);
        TimeSlot expectedPreviousTimeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // when
        TimeSlot actual = timeSlotToGetPreviousFrom.previous();

        // then
        assertThat(actual).isEqualTo(expectedPreviousTimeSlot);
    }

    @Test
    void canCheckIfTimeSlotContainsTimestamp() {
        // given
        TimeSlot timeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // when
        boolean containsNoon_1 = timeSlot.contains(NOON.minus(1, ChronoUnit.NANOS));
        boolean containsNoon = timeSlot.contains(NOON);
        boolean containsNoon1 = timeSlot.contains(NOON.plus(1, ChronoUnit.NANOS));
        boolean containsNoon2 = timeSlot.contains(NOON.plus(2, ChronoUnit.MINUTES));
        boolean containsNoon3 = timeSlot.contains(NOON.plus(3, ChronoUnit.MINUTES));
        boolean containsNoon4 = timeSlot.contains(NOON.plus(5, ChronoUnit.MINUTES).minus(1, ChronoUnit.NANOS));
        boolean containsNoon5 = timeSlot.contains(NOON.plus(5, ChronoUnit.MINUTES));

        // then
        assertThat(containsNoon_1).isFalse();
        assertThat(containsNoon).isTrue();
        assertThat(containsNoon1).isTrue();
        assertThat(containsNoon2).isTrue();
        assertThat(containsNoon3).isTrue();
        assertThat(containsNoon4).isTrue();
        assertThat(containsNoon5).isFalse();
    }

    @Test
    void canCheckIfTimeSlotEndsAtTimestamp() {
        // given
        TimeSlot timeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // when
        boolean endsAt4 = timeSlot.endsAt(NOON.plus(5, ChronoUnit.MINUTES).minus(1, ChronoUnit.NANOS));
        boolean endsAt5 = timeSlot.endsAt(NOON.plus(5, ChronoUnit.MINUTES));
        boolean endsAt6 = timeSlot.endsAt(NOON.plus(5, ChronoUnit.MINUTES).plus(1, ChronoUnit.NANOS));

        // then
        assertThat(endsAt4).isFalse();
        assertThat(endsAt5).isTrue();
        assertThat(endsAt6).isFalse();
    }

    @Test
    void canCheckIfTimeSlotIsBeforeTimestamp() {
        // given
        TimeSlot timeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // when
        boolean isBeforeNoonFive_1 = timeSlot.isTimeSlotBefore(NOON_FIVE.minus(1, ChronoUnit.NANOS));
        boolean isBeforeNoonFive = timeSlot.isTimeSlotBefore(NOON_FIVE);
        boolean isBeforeNoonFive1 = timeSlot.isTimeSlotBefore(NOON_FIVE.plus(1, ChronoUnit.NANOS));

        // then
        assertThat(isBeforeNoonFive_1).isFalse();
        assertThat(isBeforeNoonFive).isFalse();
        assertThat(isBeforeNoonFive1).isTrue();
    }

    @Test
    void canCheckIfTimeSlotIsAfterTimestamp() {
        // given
        TimeSlot timeSlot = TimeSlot.of(NOON_FIVE, NOON_TEN);

        // when
        boolean isAfterNoonFive_1 = timeSlot.isTimeSlotAfter(NOON_FIVE.minus(1, ChronoUnit.NANOS));
        boolean isAfterNoonFive = timeSlot.isTimeSlotAfter(NOON_FIVE);
        boolean isAfterNoonFive1 = timeSlot.isTimeSlotAfter(NOON_FIVE.plus(1, ChronoUnit.NANOS));

        // then
        assertThat(isAfterNoonFive_1).isTrue();
        assertThat(isAfterNoonFive).isFalse();
        assertThat(isAfterNoonFive1).isFalse();
    }

    @Test
    void canCreateTimeSlotFromSeed() {
        // given
        TimeSlot timeSlot = TimeSlot.of(NOON, NOON_FIVE);

        // when
        TimeSlot timeSlot_1 = TimeSlot.timeSlotThatContains(NOON.minus(1, ChronoUnit.NANOS));
        TimeSlot timeSlot0 = TimeSlot.timeSlotThatContains(NOON);
        TimeSlot timeSlot1 = TimeSlot.timeSlotThatContains(NOON.plus(1, ChronoUnit.NANOS));
        TimeSlot timeSlot2 = TimeSlot.timeSlotThatContains(NOON.plus(2, ChronoUnit.MINUTES));
        TimeSlot timeSlot3 = TimeSlot.timeSlotThatContains(NOON.plus(3, ChronoUnit.MINUTES));
        TimeSlot timeSlot4 = TimeSlot.timeSlotThatContains(NOON.plus(5, ChronoUnit.MINUTES).minus(1, ChronoUnit.NANOS));
        TimeSlot timeSlot5 = TimeSlot.timeSlotThatContains(NOON.plus(5, ChronoUnit.MINUTES));

        // then
        assertThat(timeSlot_1).isNotEqualTo(timeSlot);
        assertThat(timeSlot0).isEqualTo(timeSlot);
        assertThat(timeSlot1).isEqualTo(timeSlot);
        assertThat(timeSlot2).isEqualTo(timeSlot);
        assertThat(timeSlot3).isEqualTo(timeSlot);
        assertThat(timeSlot4).isEqualTo(timeSlot);
        assertThat(timeSlot5).isNotEqualTo(timeSlot);
    }
}
