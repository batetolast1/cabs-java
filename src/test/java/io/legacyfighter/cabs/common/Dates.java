package io.legacyfighter.cabs.common;

import java.time.LocalDateTime;
import java.time.Month;

public final class Dates {

    public static final LocalDateTime NEW_YEARS_EVE = LocalDateTime.of(2021, Month.DECEMBER, 31, 0, 0);
    public static final LocalDateTime NEW_YEAR_MORNING = LocalDateTime.of(2022, Month.JANUARY, 1, 6, 0);
    public static final LocalDateTime FRIDAY_EVENING = LocalDateTime.of(2022, Month.FEBRUARY, 18, 17, 0);
    public static final LocalDateTime SATURDAY_MORNING = LocalDateTime.of(2022, Month.FEBRUARY, 19, 6, 0);
    public static final LocalDateTime SATURDAY_EVENING = LocalDateTime.of(2022, Month.FEBRUARY, 19, 17, 0);
    public static final LocalDateTime SUNDAY_MORNING = LocalDateTime.of(2022, Month.FEBRUARY, 20, 6, 0);
    public static final LocalDateTime SATURDAY = LocalDateTime.of(2022, Month.FEBRUARY, 19, 12, 0);
    public static final LocalDateTime SUNDAY = LocalDateTime.of(2022, Month.FEBRUARY, 20, 12, 0);
    public static final LocalDateTime WORKING_DAY = LocalDateTime.of(2022, Month.FEBRUARY, 18, 12, 0);

    private Dates() {
    }
}
