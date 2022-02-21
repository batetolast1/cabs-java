package io.legacyfighter.cabs.common;

import java.time.LocalDateTime;
import java.time.Month;

public final class Dates {

    public static final LocalDateTime BEFORE_2019 = LocalDateTime.of(2018, Month.APRIL, 20, 14, 0);
    public static final LocalDateTime STANDARD_DAY = LocalDateTime.of(2022, Month.APRIL, 20, 14, 0);
    public static final LocalDateTime WEEKEND_PLUS = LocalDateTime.of(2022, Month.FEBRUARY, 20, 6, 0);
    public static final LocalDateTime WEEKEND = LocalDateTime.of(2022, Month.FEBRUARY, 19, 15, 0);
    public static final LocalDateTime NEW_YEARS_EVE = LocalDateTime.of(2022, Month.JANUARY, 1, 5, 0);

    private Dates() {
    }
}
