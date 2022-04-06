package io.legacyfighter.cabs.driverreport.travelleddistance;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.*;
import java.util.Objects;

@Embeddable
class TimeSlot {

    private static final int FIVE_MINUTES = 300;

    @Column(nullable = false)
    private Instant beginning;

    @Column(nullable = false)
    private Instant end;

    protected TimeSlot() {
    }

    private TimeSlot(Instant beginning, Instant end) {
        this.beginning = beginning;
        this.end = end;
    }

    Instant getBeginning() {
        return beginning;
    }

    public Instant getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(beginning, timeSlot.beginning) && Objects.equals(end, timeSlot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginning, end);
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "beginning=" + beginning +
                ", end=" + end +
                '}';
    }

    static TimeSlot of(Instant beginning, Instant end) {
        if (!end.isAfter(beginning)) {
            throw new IllegalArgumentException(String.format("End %s is not after beginning %s", beginning, end));
        }

        return new TimeSlot(beginning, end);
    }

    static TimeSlot timeSlotThatContains(Instant seed) {
        LocalDateTime seedStartOfDay = seed.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        LocalDateTime seedDateTime = seed.atZone(ZoneId.systemDefault()).toLocalDateTime();
        long secondsFromStartOfDay = Duration.between(seedStartOfDay, seedDateTime).toSeconds();
        long intervals = secondsFromStartOfDay / FIVE_MINUTES;
        long secondsToBeginning = intervals * FIVE_MINUTES;
        Instant beginning = seedStartOfDay.atZone(ZoneId.systemDefault()).plusSeconds(secondsToBeginning).toInstant();
        Instant end = beginning.plusSeconds(FIVE_MINUTES);
        return new TimeSlot(beginning, end);
    }

    boolean contains(Instant timestamp) {
        return !this.beginning.isAfter(timestamp) && timestamp.isBefore(this.end);
    }

    boolean endsAt(Instant timestamp) {
        return Objects.equals(this.end, timestamp);
    }

    boolean isBefore(Instant timestamp) {
        return this.end.isBefore(timestamp);
    }

    TimeSlot previous() {
        return new TimeSlot(this.beginning.minusSeconds(FIVE_MINUTES), this.end.minusSeconds(FIVE_MINUTES));
    }
}
